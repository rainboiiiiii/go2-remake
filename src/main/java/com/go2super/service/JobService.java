package com.go2super.service;

import com.go2super.database.entity.User;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.service.jobs.GalaxyUserJob;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.service.jobs.user.BuilderJob;
import com.go2super.service.jobs.user.RankJob;
import com.go2super.service.jobs.user.ShipConstructionJob;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@Getter
@Service
public class JobService {

    private static JobService instance;

    private LinkedList<GalaxyUserJob> jobs;
    private LinkedList<OfflineJob> offlineJobs;

    @Autowired
    RankService rankService;

    public JobService() {

        instance = this;

        jobs = new LinkedList<>();
        offlineJobs = new LinkedList<>();

        jobs.add(new BuilderJob());
        jobs.add(new ShipConstructionJob());

        offlineJobs.add(new RankJob());

    }

    @Scheduled(fixedDelay = 5000L)
    public void offlineTasks() {

        for (OfflineJob job : offlineJobs)
            job.run();

    }

    @Scheduled(fixedDelay = 500L)
    public void userTasks() {

        CopyOnWriteArrayList<LoggedGameUser> users = new CopyOnWriteArrayList<LoggedGameUser>(LoginService.getInstance().getGameUsers());

        for(LoggedGameUser gameUser : users) {

            User updated = gameUser.getUpdatedUser();

            if(updated != null)
                for (GalaxyUserJob galaxyUserJob : jobs)
                    galaxyUserJob.run(gameUser, updated);

        }

    }

    public static <E extends GalaxyUserJob> E getUserJob(Class<E> clazz) {
        for(GalaxyUserJob job : getInstance().getJobs())
            if(job.getClass().isAssignableFrom(clazz))
                return (E) job;
        return null;
    }

    public static <E extends OfflineJob> E getOfflineJob(Class<E> clazz) {
        for(OfflineJob job : getInstance().getOfflineJobs())
            if(job.getClass().isAssignableFrom(clazz))
                return (E) job;
        return null;
    }

    public static JobService getInstance() {
        return instance;
    }

}
