package ru.nsu.ganchenkova.todolist;

public class ToDoTask {

    private final int taskId;
    private final int userId;
    private final long creationTime;

    private boolean done;
    private String description;

    public ToDoTask(int taskId, int userId, long creationTime, String description) {
        this.taskId = taskId;
        this.userId = userId;
        this.creationTime = creationTime;
        this.description = description;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
