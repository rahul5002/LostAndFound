package com.lostandfound;

import com.lostandfound.service.LostAndFoundService;
import com.lostandfound.ui.ConsoleUI;
import com.lostandfound.util.DataSeeder;

/**
 * ┌─────────────────────────────────────────────────────────────┐
 * │           LOST & FOUND MANAGEMENT SYSTEM                    │
 * │                                                             │
 * │  Entry point. Boots the service layer, seeds sample data,   │
 * │  and launches the console UI.                               │
 * │                                                             │
 * │  Architecture Overview:                                     │
 * │  ┌──────────┐   ┌──────────────────┐   ┌───────────────┐   │
 * │  │ ConsoleUI│──▶│LostAndFoundService│──▶│  Repositories │   │
 * │  └──────────┘   └──────────────────┘   └───────────────┘   │
 * │                          │                                  │
 * │                  ┌───────┴────────┐                         │
 * │                  │ MatchingEngine │                         │
 * │                  └───────┬────────┘                         │
 * │                  ┌───────┴──────────────┐                   │
 * │                  │ NotificationService   │                   │
 * │                  └──────────────────────┘                   │
 * └─────────────────────────────────────────────────────────────┘
 *
 *  Run:   java -cp target/lostandfound.jar com.lostandfound.Main
 *  Build: mvn clean package
 */
public class Main {

    public static void main(String[] args) {

        // 1. Boot the central service (wires all repos + engines internally)
        LostAndFoundService service = new LostAndFoundService();

        // 2. Optionally seed sample data (skip with --no-seed flag)
        boolean seedData = true;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--no-seed")) {
                seedData = false;
                break;
            }
        }
        if (seedData) {
            DataSeeder.seed(service);
        }

        // 3. Launch the console UI
        ConsoleUI ui = new ConsoleUI(service);
        ui.start();
    }
}
