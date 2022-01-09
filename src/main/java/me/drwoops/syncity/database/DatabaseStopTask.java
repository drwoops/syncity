package me.drwoops.syncity.database;

import java.util.concurrent.CompletableFuture;

public class DatabaseStopTask implements DatabaseTask {
    CompletableFuture<Boolean> done;

    public DatabaseStopTask(CompletableFuture<Boolean> done) {
        this.done = done;
    }

    @Override
    public void run(Database database) {
        database.again = false;
        done.complete(Boolean.TRUE);
    }
}
