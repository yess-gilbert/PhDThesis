package io.crowbar.diagnostic.spectrum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class provides a way of creating a spectrum.
 */
public final class EditableSpectrum
extends Spectrum {
    private final ArrayList<Transaction> transactions = new ArrayList<Transaction> ();
    private final ArrayList<Probe> probes = new ArrayList<Probe> ();

    private final EditableTree tree = new EditableTree("root");
    private int probeCount = 0;

    @Override
    public EditableTree getTree () {
        return tree;
    }

    @Override
    public int getProbeCount () {
        return probeCount;
    }

    @Override
    public int getTransactionCount () {
        return transactions.size();
    }

    /**
     * @brief Retreives a transaction by id.
     * @return A transaction or null if a transaction with such id
     * does not exist.
     */
    @Override
    public Transaction getTransaction (int transactionId) {
        if (transactionId < 0 || transactionId >= transactions.size())
            return null;

        return transactions.get(transactionId);
    }

    /**
     * @brief Retreives a probe by id.
     * @return A probe or null if a probe with such id does
     * not exist/is not linked with any node in the tree.
     */
    @Override
    public Probe getProbe (int id) {
        if (id < 0 || id >= probes.size())
            return null;

        return probes.get(id);
    }

    /**
     * @brief Retreives the list of probes
     * @return A list of probes
     */
    @Override
    public List<Probe> getProbes () {
        return Collections.unmodifiableList(probes);
    }

    /**
     * @brief Adds a new transactions to the spectrum.
     * @pre this.getTransaction(transaction.getId()) == null
     */
    public void setTransaction (Transaction transaction) {
        assert (this.getTransaction(transaction.getId()) == null);
        transactions.ensureCapacity(transaction.getId() + 1);

        while (transactions.size() <= transaction.getId()) {
            transactions.add(null);
        }

        transactions.set(transaction.getId(), transaction);

        probeCount = Math.max(probeCount, transaction.size());
    }

    /**
     * @brief Adds a new probe to the spectrum.
     * @pre node.getTree() == this.getTree()
     * @pre this.getProbe(id) == null
     */
    public void setProbe (int id,
                          ProbeType type,
                          Node node) {
        assert (node.getTree() == this.getTree());
        assert (this.getProbe(id) == null);
        probes.ensureCapacity(id + 1);

        while (probes.size() <= id) {
            probes.add(null);
        }

        probeCount = Math.max(probeCount, id + 1);
        probes.set(id, new Probe(this, type, id, node.getId()));
    }
}