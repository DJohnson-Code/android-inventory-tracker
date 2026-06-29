# Warehouse Inventory Tracker

This repository contains my CS 360 mobile app project. The app is an Android inventory tracker built with Java, XML layouts, and SQLite. The final Project Three ZIP file is also included in this repository as the portfolio artifact.

## App Summary

The app I developed is called Warehouse Inventory Tracker. The goal was to create a simple mobile app that helps a user keep track of inventory items. The user can create an account, log in, add inventory items, view the item list, update quantities, delete items, and receive an SMS alert when an item reaches zero.

The main user need was straightforward inventory tracking. A warehouse worker, inventory manager, or small business owner needs to know what items they have, how many are available, and where those items are located. I tried to keep the app focused on that main workflow instead of turning it into a large business system.

## UI and Features

The app uses three main screens: a login/create account screen, an inventory grid screen, and an add item screen. The login screen supports users who already have accounts and users who need to create one. The inventory screen shows the current items in a table-style grid, with controls to increase, decrease, or delete items. The add item screen lets the user enter the item name, quantity, and location. It also includes the SMS alert option.

I tried to keep the UI simple because the app is meant to be used quickly. The user should not have to search around to figure out what to do. The screens are separated by task, so the flow is easier to follow: log in, view inventory, add or update items, and deal with alerts if needed.

## Development Approach

I started with the UI from Project Two and then built the actual functionality for Project Three. I used SQLite for the local database because the app needed to save users and inventory items even after the app was closed. I created a users table for login information and an items table for the inventory data.

My approach was to build the app in smaller pieces. First, I focused on login and account creation. Then I worked on the inventory database actions: create, read, update, and delete. After that, I added the SMS permission flow and the zero-quantity alert behavior. Building it in parts made the project easier to test and fix.

## Testing

I tested the app in the Android Emulator. I checked that a new user could create an account, that the login screen rejected incorrect credentials, and that the correct username and password opened the inventory screen. I also tested adding items, changing quantities with the plus and minus buttons, deleting items, and making sure the database kept the data after reopening the app.

Testing was important because a successful build did not automatically mean the app worked correctly. Some issues only show up when clicking through the app like a real user. I also tested the SMS permission behavior to make sure the app still worked if the user denied permission.

## Challenges

One challenge was connecting the inventory grid to real SQLite data. In Project Two, the grid was only a UI mockup with sample rows. For Project Three, I had to make the table rebuild from the database instead of just showing static information. I also had to make sure the quantity could update without going below zero and that the out-of-stock warning changed when an item reached zero.

Another challenge was SMS permissions. The app needed to request permission, handle denial, and continue working without SMS alerts. I wanted that feature to be useful without making it required for the rest of the app.

## Best Demonstration of Skills

The part I think best shows my progress is the database-backed inventory screen. It connects the UI to SQLite and supports the main actions the user needs: viewing items, adding items, updating quantities, and deleting records. That part shows the connection between design, user needs, and actual app behavior.