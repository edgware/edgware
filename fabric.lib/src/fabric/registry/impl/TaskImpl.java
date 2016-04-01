/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.QueryScope;
import fabric.registry.Task;
import fabric.registry.TaskService;
import fabric.registry.TaskSubscription;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a <code>Task</code>.
 */
public class TaskImpl extends AbstractRegistryObject implements Task {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    private String id = null;
    private int priority = 0;
    private String affiliation = null;
    private String description = null;
    private String detail = null;
    private String detailUri = null;

    protected TaskImpl() {

    }

    protected TaskImpl(String id, int priority, String affiliation, String description, String detail, String detailUri) {

        this.id = id;
        this.priority = priority;
        this.affiliation = affiliation;
        this.description = description;
        this.detail = detail;
        this.detailUri = detailUri;
    }

    @Override
    public String getAffiliation() {

        return affiliation;
    }

    @Override
    public void setAffiliation(String affiliation) {

        this.affiliation = affiliation;
    }

    @Override
    public String getDescription() {

        return description;
    }

    @Override
    public void setDescription(String description) {

        this.description = description;
    }

    @Override
    public String getDetail() {

        return detail;
    }

    @Override
    public void setDetail(String detail) {

        this.detail = detail;
    }

    @Override
    public String getDetailUri() {

        return detailUri;
    }

    @Override
    public void setDetailUri(String detailUri) {

        this.detailUri = detailUri;
    }

    @Override
    public String getId() {

        return id;
    }

    @Override
    public void setId(String id) {

        this.id = id;
    }

    @Override
    public int getPriority() {

        return priority;
    }

    @Override
    public void setPriority(int priority) {

        this.priority = priority;
    }

    @Override
    public TaskService[] getTaskServices() {

        return TaskServiceFactoryImpl.getInstance(QueryScope.DISTRIBUTED).getTaskServicesByTask(id);
    }

    @Override
    public TaskSubscription[] getTaskSubscriptions() {

        return TaskSubscriptionFactoryImpl.getInstance(QueryScope.DISTRIBUTED).getTaskSubscriptionsByTask(id);
    }

    @Override
    public void validate() throws IncompleteObjectException {

        if (id == null || id.length() == 0) {
            throw new IncompleteObjectException("Missing or invalid task id");
        }
    }

    @Override
    public String toString() {

        StringBuffer buffy = new StringBuffer("Task::");
        buffy.append(" Task ID: ").append(id);
        buffy.append(", Priority: ").append(priority);
        buffy.append(", Affiliation: ").append(affiliation);
        buffy.append(", Description: ").append(description);
        buffy.append(", Task Detail: ").append(detail);
        buffy.append(", Task Detail URI: ").append(detailUri);
        return buffy.toString();
    }

    @Override
    public boolean equals(Object obj) {

        boolean equal = false;
        if (obj != null && obj instanceof Task) {
            Task task = (Task) obj;
            if ((task.getId() == null ? id == null : task.getId().equals(id))
                    && task.getPriority() == priority
                    && (task.getAffiliation() == null ? affiliation == null : task.getAffiliation().equals(affiliation))
                    && (task.getDescription() == null ? description == null : task.getDescription().equals(description))
                    && (task.getDetail() == null ? detail == null : task.getDetail().equals(detail))
                    && (task.getDetailUri() == null ? detailUri == null : task.getDetailUri().equals(detailUri))) {

                equal = true;
            }
        }
        return equal;
    }

    @Override
    public String key() {

        return this.getId();
    }
}
