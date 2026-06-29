package com.example.darnelljohnsoninventoryapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    private DatabaseHelper databaseHelper;
    private boolean smsAlertsEnabled = false;
    private String alertPhoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);
        showLoginScreen();
    }

    private void showLoginScreen() {
        setContentView(R.layout.activity_login);
    }

    private void showInventoryScreen() {
        setContentView(R.layout.activity_inventory);
        loadInventoryTable();
    }

    private void showAddItemScreen() {
        setContentView(R.layout.activity_add_item);

        EditText phoneNumber = findViewById(R.id.editPhoneNumber);
        TextView statusText = findViewById(R.id.textSmsStatus);

        phoneNumber.setText(alertPhoneNumber);
        statusText.setText(smsAlertsEnabled ? "SMS alerts are on" : "SMS alerts are off");
    }

    public void loginUser(View view) {
        EditText usernameInput = findViewById(R.id.editUsername);
        EditText passwordInput = findViewById(R.id.editPassword);

        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUser(username, password)) {
            Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show();
            showInventoryScreen();
        } else {
            Toast.makeText(this, "Login failed. Check username and password.", Toast.LENGTH_SHORT).show();
        }
    }

    public void createAccount(View view) {
        EditText usernameInput = findViewById(R.id.editUsername);
        EditText passwordInput = findViewById(R.id.editPassword);

        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.userExists(username)) {
            Toast.makeText(this, "Username already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.addUser(username, password)) {
            Toast.makeText(this, "Account created.", Toast.LENGTH_SHORT).show();
            showInventoryScreen();
        } else {
            Toast.makeText(this, "Account could not be created.", Toast.LENGTH_SHORT).show();
        }
    }

    public void openAddItem(View view) {
        showAddItemScreen();
    }

    public void saveItem(View view) {
        EditText itemNameInput = findViewById(R.id.editItemName);
        EditText quantityInput = findViewById(R.id.editQuantity);
        EditText locationInput = findViewById(R.id.editLocationDescription);

        String itemName = itemNameInput.getText().toString().trim();
        String quantityText = quantityInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        if (itemName.isEmpty() || quantityText.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Enter item name, quantity, and location.", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantity < 0) {
            Toast.makeText(this, "Quantity cannot be negative.", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = databaseHelper.addItem(itemName, quantity, location);

        if (result != -1) {
            Toast.makeText(this, "Item saved.", Toast.LENGTH_SHORT).show();

            if (quantity == 0) {
                sendZeroQuantityAlert(itemName);
            }

            showInventoryScreen();
        } else {
            Toast.makeText(this, "Item could not be saved.", Toast.LENGTH_SHORT).show();
        }
    }

    public void returnToInventory(View view) {
        showInventoryScreen();
    }

    public void enableSmsAlerts(View view) {
        EditText phoneNumberInput = findViewById(R.id.editPhoneNumber);
        TextView statusText = findViewById(R.id.textSmsStatus);

        alertPhoneNumber = phoneNumberInput.getText().toString().trim();

        if (alertPhoneNumber.isEmpty()) {
            Toast.makeText(this, "Enter a phone number for SMS alerts.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            smsAlertsEnabled = true;
            statusText.setText("SMS alerts are on");
            Toast.makeText(this, "SMS alerts enabled.", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE
            );
        }
    }

    public void declineSmsAlerts(View view) {
        smsAlertsEnabled = false;
        TextView statusText = findViewById(R.id.textSmsStatus);
        statusText.setText("SMS alerts are off");
        Toast.makeText(this, "SMS alerts are off.", Toast.LENGTH_SHORT).show();
    }

    private void loadInventoryTable() {
        TableLayout inventoryTable = findViewById(R.id.tableInventory);
        TextView stockWarning = findViewById(R.id.textStockWarning);

        // Keep the header row from the XML and rebuild the data rows from SQLite.
        while (inventoryTable.getChildCount() > 1) {
            inventoryTable.removeViewAt(1);
        }

        StringBuilder outOfStockItems = new StringBuilder();
        Cursor cursor = databaseHelper.getAllItems();

        if (cursor.getCount() == 0) {
            addEmptyInventoryRow(inventoryTable);
            stockWarning.setText("No out-of-stock items");
        } else {
            while (cursor.moveToNext()) {
                int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ITEM_ID));
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ITEM_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ITEM_QUANTITY));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ITEM_LOCATION));

                addInventoryRow(inventoryTable, itemId, itemName, quantity, location);

                if (quantity == 0) {
                    if (outOfStockItems.length() > 0) {
                        outOfStockItems.append(", ");
                    }
                    outOfStockItems.append(itemName);
                }
            }

            if (outOfStockItems.length() > 0) {
                stockWarning.setText("Out of stock: " + outOfStockItems);
            } else {
                stockWarning.setText("No out-of-stock items");
            }
        }

        cursor.close();
    }

    private void addEmptyInventoryRow(TableLayout inventoryTable) {
        TableRow row = new TableRow(this);

        TextView emptyText = makeCellText("No inventory items yet.");
        row.addView(emptyText);
        row.addView(makeCellText(""));
        row.addView(makeCellText(""));
        row.addView(makeCellText(""));

        inventoryTable.addView(row);
    }

    private void addInventoryRow(TableLayout inventoryTable, int itemId, String itemName, int quantity, String location) {
        TableRow row = new TableRow(this);

        row.addView(makeCellText(itemName));
        row.addView(makeCenteredCellText(String.valueOf(quantity)));
        row.addView(makeCellText(location));
        row.addView(makeActionButtons(itemId, itemName, quantity));

        inventoryTable.addView(row);
    }

    private TextView makeCellText(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.parseColor("#1F2933"));
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private TextView makeCenteredCellText(String text) {
        TextView textView = makeCellText(text);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private LinearLayout makeActionButtons(int itemId, String itemName, int quantity) {
        LinearLayout actionLayout = new LinearLayout(this);
        actionLayout.setOrientation(LinearLayout.VERTICAL);

        Button increaseButton = makeSmallButton("+");
        increaseButton.setOnClickListener(v -> updateItemQuantity(itemId, itemName, quantity + 1));

        Button decreaseButton = makeSmallButton("-");
        decreaseButton.setOnClickListener(v -> {
            int newQuantity = Math.max(quantity - 1, 0);
            updateItemQuantity(itemId, itemName, newQuantity);
        });

        Button deleteButton = makeSmallButton("Delete");
        deleteButton.setBackgroundColor(Color.parseColor("#7A2E2E"));
        deleteButton.setOnClickListener(v -> {
            databaseHelper.deleteItem(itemId);
            Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
            loadInventoryTable();
        });

        actionLayout.addView(increaseButton);
        actionLayout.addView(decreaseButton);
        actionLayout.addView(deleteButton);

        return actionLayout;
    }

    private Button makeSmallButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.parseColor("#2F5D50"));
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setPadding(8, 4, 8, 4);
        return button;
    }

    private void updateItemQuantity(int itemId, String itemName, int newQuantity) {
        databaseHelper.updateQuantity(itemId, newQuantity);

        if (newQuantity == 0) {
            sendZeroQuantityAlert(itemName);
        }

        loadInventoryTable();
    }

    private void sendZeroQuantityAlert(String itemName) {
        if (!smsAlertsEnabled) {
            Toast.makeText(this, itemName + " reached zero. SMS alerts are off.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (alertPhoneNumber.isEmpty()) {
            Toast.makeText(this, "No phone number saved for SMS alerts.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = getSystemService(SmsManager.class);
            if (smsManager == null) {
                Toast.makeText(this, "SMS service is not available.", Toast.LENGTH_SHORT).show();
                return;
            }

            String message = "Inventory alert: " + itemName + " has reached zero.";
            smsManager.sendTextMessage(alertPhoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS alert sent for " + itemName + ".", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS could not be sent in this environment.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            TextView statusText = findViewById(R.id.textSmsStatus);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsAlertsEnabled = true;
                statusText.setText("SMS alerts are on");
                Toast.makeText(this, "SMS alerts enabled.", Toast.LENGTH_SHORT).show();
            } else {
                smsAlertsEnabled = false;
                statusText.setText("SMS alerts are off");
                Toast.makeText(this, "SMS permission denied. App will still work without alerts.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
