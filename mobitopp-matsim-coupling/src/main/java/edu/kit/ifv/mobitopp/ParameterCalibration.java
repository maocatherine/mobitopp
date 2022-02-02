package edu.kit.ifv.mobitopp;

import edu.kit.ifv.mobitopp.util.randomvariable.DiscreteRandomVariable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class ParameterCalibration {

    public static void main(String[] args) throws IOException {
        String basePath = "C:\\Users\\lfen0003\\IdeaProjects\\mobitopp\\mobitopp-matsim-coupling\\data_clayton\\parameters\\calibration\\";
        String finalPath = basePath + "destinationwalk.csv";
        File csv = new File(finalPath);
        double rand = new Random(1234).nextDouble();
        StringBuilder finalChoice = new StringBuilder();
        finalChoice.append("id,CHOICE");
        finalChoice.append("\n");

        try (Scanner scanner = new Scanner(csv)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] lineParts = line.split(";");
                Map<String, Double> map = new HashMap<>();

                for (int i = 0; i < 150; i++) {
                    String id = Integer.toString(i);
                    map.put(id, Double.parseDouble(lineParts[i+1]));
                }
                finalChoice.append(Integer.parseInt(lineParts[0]));
                finalChoice.append(",");
                finalChoice.append(select(map, rand));
                finalChoice.append("\n");
            }
        }

        try (PrintWriter writer = new PrintWriter("C:\\Users\\lfen0003\\IdeaProjects\\mobitopp\\mobitopp-matsim-coupling\\output\\destchoicewalkresult.csv")) {

            writer.write(finalChoice.toString());

            System.out.println("done!");

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }


    }

    public static String select(
            Map<String, Double> probabilities,
            Double randomNumber
    ) {
        DiscreteRandomVariable<String> distribution = new DiscreteRandomVariable<String>(probabilities);
        return distribution.realization(randomNumber);
    }


}
