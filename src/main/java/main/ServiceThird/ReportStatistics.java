package main.ServiceThird;

import java.util.ArrayList;
import java.util.List;

public class ReportStatistics {
    private ArrayList<ArrivedShip> shipsList;
    private transient int[] shipsDelays;
    //PRODUCTIVITY CONFIG
    private int containerProductivityConfig;
    private int fluidProductivityConfig;
    private int friableProductivityConfig;
    //SHIPS CONFIG
    private int numberOfShips;
    private int numberOfContainerShips;
    private int numberOfFluidShips;
    private int numberOfFriableShips;
    //STATISTICS
    //PENALTIES
    private int containerPenalty;
    private int fluidPenalty;
    private int friablePenalty;
    private int allPenalty;
    //SHIPS
    private int unloadedShips;
    private double averageWaitTime;
    private double averageQueueLength;
    private double averageUnloadDelay;
    private int maxUnloadDelay;
    //CRANES
    private int totalContainerCranes;
    private int totalFluidCranes;
    private int totalFriableCranes;

    public ReportStatistics(List<ArrivedShip> shipsList, int[] shipsDelays,
                            int containerProd, int fluidProd, int friableProd,
                            int numberOfShips, int numberOfContainerShips,
                            int numberOfFluidShips, int numberOfFriableShips,
                            int containerCranes, int fluidCranes, int friableCranes) {

        this.shipsList = (ArrayList<ArrivedShip>) shipsList;
        this.shipsDelays = shipsDelays;

        this.containerProductivityConfig = containerProd;
        this.fluidProductivityConfig = fluidProd;
        this.friableProductivityConfig = friableProd;

        this.numberOfShips = numberOfShips;
        this.numberOfContainerShips = numberOfContainerShips;
        this.numberOfFluidShips = numberOfFluidShips;
        this.numberOfFriableShips = numberOfFriableShips;

        this.totalContainerCranes = containerCranes;
        this.totalFluidCranes = fluidCranes;
        this.totalFriableCranes = friableCranes;
    }

    public void collectStatistics(int containerPenalty, int fluidPenalty,
                                  int friablePenalty, int allPenalty, int unloadedShips,
                                  double averageWaitTime, double averageQueueLength,
                                  double averageUnloadDelay, int maxUnloadDelay) {

        this.containerPenalty = containerPenalty;
        this.fluidPenalty = fluidPenalty;
        this.friablePenalty = friablePenalty;
        this.allPenalty = allPenalty;
        this.unloadedShips = unloadedShips;
        this.averageWaitTime = averageWaitTime;
        this.averageQueueLength = averageQueueLength;
        this.averageUnloadDelay = averageUnloadDelay;
        this.maxUnloadDelay = maxUnloadDelay;
    }
}
