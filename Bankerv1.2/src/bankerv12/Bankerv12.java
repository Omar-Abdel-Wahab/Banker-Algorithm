package bankerv12;

import java.util.*;

public class Bankerv12 {

    private static int resourcesTypes;
    private static int numberOfProcesses;
    private static int[] instancesPerResource;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of resource types:");
        resourcesTypes = sc.nextInt();
        instancesPerResource = new int[resourcesTypes];

        System.out.println("Enter number of resources instances for each type:");
        for (int i = 0; i < instancesPerResource.length; i++) {
            instancesPerResource[i] = sc.nextInt();
        }
        System.out.println("Enter number of processes:");
        numberOfProcesses = sc.nextInt();
        System.out.println("");

        Process ps[] = new Process[numberOfProcesses];
        Random r = new Random();
        ArrayList<Process> processesList = new ArrayList();
        boolean terminated;
        
        int[] copyOfInstancesPerResource =  new int[resourcesTypes];
        System.arraycopy(instancesPerResource, 0, copyOfInstancesPerResource, 0, 
                resourcesTypes);

        for (int i = 0; i < numberOfProcesses; i++) {
            ps[i] = createProcess(i, copyOfInstancesPerResource);
            processesList.add(ps[i]);
            System.out.println(ps[i]);
        }
        
        int numberOfArrayListElements = processesList.size();
        System.out.println("Now available: ");
        for (int i = 0; i < resourcesTypes - 1; i++) {
            System.out.print(instancesPerResource[i] + " ");
        }
        System.out.println(instancesPerResource[resourcesTypes - 1]);

        do {
            Process p = selectProcess(processesList);
            if (p == null) {
                System.out.println("Unsafe state. All processes requests are denied. "
                        + "Exiting now");
                System.exit(0);
            } else {
                System.out.println("Process P" + p.getNumberOfProcess() + " is executing");
                System.out.println(p);
                String s = "";
                for (int i = 0; i < resourcesTypes - 1; i++) {
                    s += instancesPerResource[i] + " ";
                }
                s += instancesPerResource[resourcesTypes - 1];
                System.out.println("Now available: " + s);
                int allocated[] = p.getAllocatedResourcesInstances();
                int[] previousAllocation = new int[resourcesTypes];
                int[] currentAllocation = new int [resourcesTypes];
                int retrievedAllocation;
                for (int i = 0; i < resourcesTypes; i++) {
                    previousAllocation[i] = allocated[i];
                    currentAllocation[i] = r.nextInt(allocated[i] + 1);
                    retrievedAllocation = previousAllocation[i] - currentAllocation[i];
                    instancesPerResource[i] += retrievedAllocation;
                }
                p.setAllocatedResourcesInstances(currentAllocation);
                terminated = checkProcessNeeds(p);
                if (terminated) {
                    numberOfArrayListElements--;
                    processesList.remove(p);
                    for(int i = 0; i < resourcesTypes; i++){
                        instancesPerResource[i] += currentAllocation[i];
                    }
                } else {
                    processesList.remove(p);
                    p = makeNewRequest(p);
                    processesList.add(p);
                }
                System.out.println("New Request for P" + p.getNumberOfProcess() + "\t" + p);
                s = "";
                for (int i = 0; i < resourcesTypes - 1; i++) {
                    s += instancesPerResource[i] + " ";
                }
                s += instancesPerResource[resourcesTypes - 1];
                System.out.println("After Release: " + s);
            }
        } while (numberOfArrayListElements > 0);

    }

    public static Process selectProcess(ArrayList al) {
        Process p = (Process) al.get(0);
        boolean safe = true;
        for (int i = 0; i < al.size(); i++) {
            p = (Process) al.get(i);
            int allocated[] = p.getAllocatedResourcesInstances();
            int needed[] = p.getNeededResourcesInstances();
            int requested[] = p.getRequestedResourcesInstances();
            for (int j = 0; j < resourcesTypes; j++) {
                if (requested[j] > instancesPerResource[j]) {
                    safe = false;
                    break;
                }
            }
            if (!safe) {
                System.out.println("Request for P" + p.getNumberOfProcess() + " is denied");
                if(i == al.size() - 1){
                    safe = false;
                }
                else{
                safe = true;    
                }
            } else {
                for (int k = 0; k < resourcesTypes; k++) {
                    needed[k] -= requested[k];
                    instancesPerResource[k] -= requested[k];
                    allocated[k] += requested[k];
                    requested[k] = 0;
                }
                p.setAllocatedResourcesInstances(allocated);
                p.setNeededResourcesInstances(needed);
                p.setRequestedResourcesInstances(requested);
                break;
            }
        }
        if (safe) {
            return p;
        } else {
            return null;
        }
    }

    public static boolean checkProcessNeeds(Process p) {
        int[] needed = p.getNeededResourcesInstances();
        for (int i = 0; i < needed.length; i++) {
            if (needed[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static Process makeNewRequest(Process p) {
        int[] requested = p.getRequestedResourcesInstances();
        int[] needed = p.getNeededResourcesInstances();
        Random r = new Random();
        for (int i = 0; i < needed.length; i++) {
            requested[i] = r.nextInt(needed[i] + 1);
        }
        p.setRequestedResourcesInstances(requested);
        return p;
    }
    
    public static Process createProcess(int i, int[] copyOfInstancesPerResource){
        Process p;
        int allocatedResources[] = new int[resourcesTypes];
        int maxResources[] = new int[resourcesTypes];
        int neededResources[] = new int[resourcesTypes];
        int requestedResources[] = new int[resourcesTypes];
        Random r = new Random();
        
        for (int j = 0; j < resourcesTypes; j++) {
                allocatedResources[j] = r.nextInt(instancesPerResource[j] / 2);
                maxResources[j] = allocatedResources[j]
                        + r.nextInt(copyOfInstancesPerResource[j] - 2 * 
                                allocatedResources[j] + 1);
                neededResources[j] = maxResources[j] - allocatedResources[j];
                requestedResources[j] = r.nextInt(neededResources[j] + 1);
                instancesPerResource[j] -= allocatedResources[j];
            }
        p = new Process(i, allocatedResources, maxResources, neededResources, 
                requestedResources);
        return p;
    }
}

class Process {

    private int numberOfProcess;
    private int[] allocatedResourcesInstances;
    private int[] maxResourcesInstances;
    private int[] neededResourcesInstances;
    private int[] requestedResourcesInstances;

    public Process(int numberOfProcess, int[] allocatedResourcesInstances,
            int[] maxResourcesInstances, int[] neededResourcesInstances,
            int[] requestedResourcesInstances) {
        this.numberOfProcess = numberOfProcess;
        this.allocatedResourcesInstances = allocatedResourcesInstances;
        this.maxResourcesInstances = maxResourcesInstances;
        this.neededResourcesInstances = neededResourcesInstances;
        this.requestedResourcesInstances = requestedResourcesInstances;
    }

    public int getNumberOfProcess() {
        return numberOfProcess;
    }

    public int[] getAllocatedResourcesInstances() {
        return allocatedResourcesInstances;
    }

    public int[] getMaxResourcesInstances() {
        return maxResourcesInstances;
    }

    public int[] getNeededResourcesInstances() {
        return neededResourcesInstances;
    }

    public int[] getRequestedResourcesInstances() {
        return requestedResourcesInstances;
    }

    public void setNumberOfProcess(int numberOfProcess) {
        this.numberOfProcess = numberOfProcess;
    }

    public void setAllocatedResourcesInstances(int[] allocatedResourcesInstances) {
        this.allocatedResourcesInstances = allocatedResourcesInstances;
    }

    public void setMaxResourcesInstances(int[] maxResourcesInstances) {
        this.maxResourcesInstances = maxResourcesInstances;
    }

    public void setNeededResourcesInstances(int[] neededResourcesInstances) {
        this.neededResourcesInstances = neededResourcesInstances;
    }

    public void setRequestedResourcesInstances(int[] requestedResourcesInstances) {
        this.requestedResourcesInstances = requestedResourcesInstances;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < allocatedResourcesInstances.length - 1; i++) {
            s = s + allocatedResourcesInstances[i] + " ";
        }
        s += allocatedResourcesInstances[allocatedResourcesInstances.length - 1];
        s += "\t";
        for (int i = 0; i < maxResourcesInstances.length - 1; i++) {
            s = s + maxResourcesInstances[i] + " ";
        }
        s += maxResourcesInstances[maxResourcesInstances.length - 1];
        s += "\t";
        for (int i = 0; i < neededResourcesInstances.length - 1; i++) {
            s = s + neededResourcesInstances[i] + " ";
        }
        s += neededResourcesInstances[neededResourcesInstances.length - 1];
        s += "\t";
        for (int i = 0; i < requestedResourcesInstances.length - 1; i++) {
            s = s + requestedResourcesInstances[i] + " ";
        }
        s += requestedResourcesInstances[requestedResourcesInstances.length - 1];
        return s;
    }
}
