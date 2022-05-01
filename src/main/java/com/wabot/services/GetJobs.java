package com.wabot.services;

import com.wabot.model.Job;
import com.wabot.model.Sqlite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetJobs extends Service<ObservableList<Job>> {

    public static final String query = "SELECT * FROM job";
    private final ObservableList<Job> jobs = FXCollections.observableArrayList();

    @Override
    protected Task<ObservableList<Job>> createTask() {
        return new Task<>() {
            @Override
            protected ObservableList<Job> call() {
                try (PreparedStatement statement = Sqlite.getInstance().getConnection().prepareStatement("INSERT INTO job (date) values ('added'), ('newwww')")
                ) {
                    statement.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    throw new RuntimeException(e);
                }
                try (PreparedStatement statement = Sqlite.getInstance().getConnection().prepareStatement(query);
                     ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        jobs.add(new Job()
                                .setId(result.getInt("id"))
                                .setDate(result.getString("date"))
                        );
                    }
                    return jobs;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
