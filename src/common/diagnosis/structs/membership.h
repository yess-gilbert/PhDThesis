#ifndef __DIAGNOSIS_STRUCTS_MEMBERSHIP_H__
#define __DIAGNOSIS_STRUCTS_MEMBERSHIP_H__

#include "types.h"
#include "utils/boost.h"

#include <iostream>

namespace diagnosis {
namespace structs {

  typedef double t_membership_value;
  typedef boost::shared_ptr<t_membership_value[]> t_membership_value_ptr;
  
  class t_membership {
  public:
    t_membership() {}
    
    t_membership(t_count points, t_count centroids) :
      num_points(points),
      num_centroids(centroids) {
      this->memberships = 
        t_membership_value_ptr( new t_membership_value[num_points * num_centroids]);
    }
    
    void set_membership(t_id point, 
                        t_id centroid, 
                        t_membership_value membership = 0, 
                        t_id offset = 1);
    
    t_membership_value get_membership(t_id point, 
                                      t_id centroid, 
                                      t_id offset = 1) const;
    
    std::ostream & print(std::ostream & out) const;
    
    t_count get_num_points() const;
    
    t_count get_num_centroids() const;
    
  private:
    t_count num_points, num_centroids;
    t_membership_value_ptr memberships;
  };

  std::ostream & operator << (std::ostream & out, const t_membership & memberships);

}
}

#endif
