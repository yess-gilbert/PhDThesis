#include "benchmark.h"

#include <algorithm>
#include <cassert>
#include <cmath>
#include <boost/foreach.hpp>

using namespace diagnosis::structs;
using namespace diagnosis::randomizers;

namespace diagnosis {
namespace benchmark {
class t_generator_job : public t_job {
public:
    t_generator_job (t_id generator_id,
                     const t_benchmark_settings & settings,
                     const t_const_ptr<t_status_iteration_init> & status);

    void operator () () const;
    std::string get_type () const;
    virtual bool operator < (const t_job & job) const;

private:
    t_id generator_id;
    const t_benchmark_settings & settings;
    const t_const_ptr<t_status_iteration_init> status;
};

class t_ranker_job : public t_job {
public:
    t_ranker_job (t_id ranker_id,
                  const t_benchmark_settings & settings,
                  const t_const_ptr<t_status_post_gen> & status);

    void operator () () const;
    std::string get_type () const;
    virtual bool operator < (const t_job & job) const;

private:
    t_id ranker_id;
    const t_benchmark_settings & settings;
    const t_const_ptr<t_status_post_gen> status;
};

t_generator_job::t_generator_job (t_id generator_id,
                                  const t_benchmark_settings & settings,
                                  const t_const_ptr<t_status_iteration_init> & status) : generator_id(generator_id), settings(settings), status(status) {}

void t_generator_job::operator () () const {
    const t_const_ptr<t_candidate_generator> & generator = settings.get_generator(generator_id);
    const t_benchmark_settings::t_ranker_list & connections = settings.get_connections(generator_id);


    if (connections.size() == 0)
        return;

    t_candidate_generator::t_ret_type * candidates_tmp =
        new t_candidate_generator::t_ret_type();

    t_const_ptr<t_candidate_generator::t_ret_type> candidates(candidates_tmp);

    t_time_interval start_time = time_interval();
    (* generator)(status->get_spectra(), * candidates_tmp);

    // Hook: Post-gen
    t_const_ptr<t_status_post_gen> gen_status
        (new t_status_post_gen(*status,
                               settings.get_generator_name(generator_id),
                               start_time,
                               time_interval(),
                               candidates));
    settings.get_hook().post_gen(settings.get_collector(),
                                 *gen_status);


    BOOST_FOREACH(t_id ranker_id, connections) {
        t_const_ptr<t_job> job(new t_ranker_job(ranker_id,
                                                settings,
                                                gen_status));


        settings.get_job_queue().add_job(job);
    }
}

std::string t_generator_job::get_type () const {
    return "generator: " + settings.get_generator_name(generator_id);
}

bool t_generator_job::operator < (const t_job & job) const {
    if (dynamic_cast<const t_ranker_job *> (&job))
        return true;

    return false;
}

t_ranker_job::t_ranker_job (t_id ranker_id,
                            const t_benchmark_settings & settings,
                            const t_const_ptr<t_status_post_gen> & status) : ranker_id(ranker_id), settings(settings), status(status) {}


void t_ranker_job::operator () () const {
    const t_const_ptr<t_candidate_ranker> & ranker = settings.get_ranker(ranker_id);

    t_candidate_ranker::t_ret_type * probs_ptr(new t_candidate_ranker::t_ret_type());


    t_const_ptr<t_candidate_ranker::t_ret_type> probs(probs_ptr);


    t_time_interval start_time = time_interval();


    (* ranker)(status->get_spectra(),
               status->get_candidates(),
               * probs_ptr);

    // Hook: Post-rank
    t_status_post_rank rank_status(*status,
                                   settings.get_ranker_name(ranker_id),
                                   start_time,
                                   time_interval(),
                                   probs);
    settings.get_hook().post_rank(settings.get_collector(),
                                  rank_status);
}

std::string t_ranker_job::get_type () const {
    return "ranker: " + settings.get_ranker_name(ranker_id);
}

bool t_ranker_job::operator < (const t_job & job) const {
    return false;
}

void run_benchmark (randomizers::t_architecture & arch,
                    boost::random::mt19937 & gen,
                    t_benchmark_settings & settings,
                    t_execution_controller & controller) {
    t_id sys_id = 1;


    while (true) {
        t_system * system = arch(gen);
        t_id iteration_id = 1;

        if (system == NULL)
            break;

        // Hook: System Init
        t_status_system_init sys_status(sys_id++);
        settings.get_hook().init_system(settings.get_collector(),
                                        *system);

        while (true) {
            t_candidate * correct_tmp = new t_candidate();
            t_const_ptr<t_candidate> correct(correct_tmp);
            t_const_ptr<t_spectra> spectra((* system)(gen, * correct_tmp));

            if (spectra.get() == NULL)
                break;

            // Hook: Iteration Init
            t_const_ptr<t_status_iteration_init> it_status
                (new t_status_iteration_init(sys_status,
                                             iteration_id++,
                                             time_interval(),
                                             spectra,
                                             correct));
            settings.get_hook().init(settings.get_collector(),
                                     *it_status);

            for (t_id gen_id = 1;
                 gen_id <= settings.get_generator_count();
                 gen_id++) {
                t_const_ptr<t_job> job(new t_generator_job(gen_id,
                                                           settings,
                                                           it_status));

                settings.get_job_queue().add_job(job);
            }

            settings.get_job_queue().execute(controller,
                                             true);
        }

        delete system;
    }

    settings.get_job_queue().execute(controller,
                                     false);
}
}
}