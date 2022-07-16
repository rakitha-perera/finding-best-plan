package com.assessment.findbestplan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FindBestPlanApplication {

    static List<Plan> plans = new ArrayList<>();
    static Packages packages = new Packages();

    public static void main(String[] args) throws IOException {
        Path path = Paths.get(args[0]);
        List<String> servicePlanRecords = Files.readAllLines(path);

        //Get all the records into a list and sort by no of services they provide
        for (String record : servicePlanRecords) {
            String[] params = record.split(",", 3);
            Plan plan = new Plan(params[0], Integer.parseInt(params[1]), params[2]);
            plans.add(plan);
        }
        plans.sort(new PlanSort());

        //Find the combination of plans that satisfies the conditions and add to a list of packages
        for (int i = 0; i < plans.size(); i++) {
            Package packageToAdd = new Package();
            Set<String> requestedServices = Arrays.stream(args[1].split(",")).collect(Collectors.toSet());
            findPlan(i, plans.size(), requestedServices, packageToAdd);
        }

        //Get the cheapest package from the list
        Package minPackage = packages.getMinPackage();
        String title = minPackage.getPackageName();

        System.out.println(minPackage.getCost() + "," + title);
    }

    public static void findPlan(int start, int end, Set<String> requestedServices, Package packageToAdd) {
        if (start == end) {
            return;
        }

        //remove the satisfied services from the list
        if (requestedServices.removeAll(plans.get(start).services)) {
            packageToAdd.plans.add(plans.get(start));
        }

        //this means all the services are satisfied, add to package and return
        if (requestedServices.size() == 0) {
            packages.packages.add(packageToAdd);
            return;
        }

        findPlan(start + 1, end, requestedServices, packageToAdd);
    }

    public static class Packages {
        List<Package> packages = new ArrayList<>();

        public Package getMinPackage() {
            int minCost = packages.get(0).getCost();
            int minIndex = 0;

            for (int i = 0; i < packages.size(); i++) {
                if (packages.get(i).getCost() < minCost) {
                    minCost = packages.get(i).getCost();
                    minIndex = i;
                }
            }

            return packages.get(minIndex);
        }
    }

    public static class Package {
        List<Plan> plans = new ArrayList<>();

        public String getPackageName() {
            return plans.stream().map(plan -> plan.planName).collect(Collectors.joining(","));
        }

        public int getCost() {
            AtomicInteger cost = new AtomicInteger();
            plans.forEach(plan -> {
                cost.set(cost.get() + plan.amount);
            });
            return cost.get();
        }
    }

    public static class Plan {
        public String planName;
        public int amount;
        public Set<String> services;

        public Plan(String planName, int amount, String services) {
            this.planName = planName;
            this.amount = amount;
            this.services = Arrays.stream(services.split(",")).collect(Collectors.toSet());
        }
    }

    public static class PlanSort implements Comparator<Plan> {
        public int compare(Plan a, Plan b) {
            return b.services.size() - a.services.size();
        }
    }
}
