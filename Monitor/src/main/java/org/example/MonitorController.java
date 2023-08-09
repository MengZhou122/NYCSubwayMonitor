package org.example;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MonitorController {
    List<String> allLineList = new ArrayList<>();
    Map<String, Double> delayRecord = new HashMap<>();
    List<String> delayedLineList = new ArrayList<>();

    Crawler crawler = new Crawler();
    StringBuilder context = new StringBuilder();

    public void updateContext() {
        crawler.crawl();
        List<String> updatedAllLineList = crawler.getAllLines();
        if (updatedAllLineList.size() == 0) return;
        List<String> updatedDelayedLineList = crawler.getDelayedLines();

        delayRecord.put("Count", delayRecord.getOrDefault("Count", 0.0) + 1);

        context = new StringBuilder();
        //log new delayed lines
        for (String delayedLine: updatedDelayedLineList) {
            delayRecord.put(delayedLine, delayRecord.getOrDefault(delayedLine, 0.0) + 1);

            if (!delayedLineList.contains(delayedLine)) {
                context.append("<br>Line").append(delayedLine).append("is experiencing delays.");
            }
        }

        //log recovered lines
        for (String delayedLine: delayedLineList) {
            if (!updatedDelayedLineList.contains(delayedLine)) {
                context.append("Line").append(delayedLine).append("is now recovered.");
            }
        }

        allLineList = updatedAllLineList;
        delayedLineList = updatedDelayedLineList;
    }

    //home page
    @GetMapping("/")
    public String home() {
        updateContext();
        String allLines = String.join(" ", allLineList);
        String delayedLines = String.join(" ", delayedLineList);
        return "All subway line list:<br>" + allLines + "<br>" + "Delayed line list:<br>" + delayedLines;
    }

    // Scheduled to refresh every 10 seconds
    @Scheduled(fixedRate = 1000)
    public void refresh() {
        System.out.println("Delay information Updates:");
        updateContext();
        System.out.println(context.toString());
    }

    //get the uptime for line $linenumber
    @GetMapping("/uptime/{linenumber}")
    public String uptime(@PathVariable String linenumber) {
        if (linenumber == null || linenumber.length() == 0 || !allLineList.contains(linenumber)) {
            return linenumber + " is not a valid line number.<br>Please input the correct number of line you want to check!";
        }

        double delayedTimes = delayRecord.getOrDefault(linenumber, 0.0);
        double uptime = 1 - delayedTimes / delayRecord.get("Count");

        return "The uptime for line: " + linenumber + " is : " + uptime;
    }

    //get the status for line $linenumber
    @GetMapping("/status/{linenumber}")
    public String status(@PathVariable String linenumber) {
        if (linenumber == null || linenumber.length() == 0 || !allLineList.contains(linenumber)) {
            return linenumber + " is not a valid line number.<br>Please input the correct number of line you want to check!";
        }

        String status = delayedLineList.contains(linenumber) ? "Delayed": "Not Delayed";
        return "The status for line: " + linenumber + " is : " + status;
    }
}
