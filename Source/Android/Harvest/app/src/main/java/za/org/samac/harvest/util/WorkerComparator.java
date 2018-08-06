package za.org.samac.harvest.util;

import java.util.Comparator;

import za.org.samac.harvest.domain.Worker;

public class WorkerComparator implements Comparator<Worker> {
    @Override
    public int compare(Worker a, Worker b) {
        return a.getSurname().compareToIgnoreCase(b.getSurname());
    }
}
