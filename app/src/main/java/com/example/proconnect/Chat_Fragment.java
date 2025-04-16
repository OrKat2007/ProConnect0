package com.example.proconnect;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.proconnect.adapters.MessageAdapter;
import com.example.proconnect.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.List;

public class Chat_Fragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private FirebaseFirestore db;
    private RecyclerView messagesRecyclerView;
    private EditText etMessage;
    private Button btnSend;
    private TextView tvproffessionalname;
    private String chatId;
    private String currentUserEmail;
    private String currentUserName; // current user's display name
    private String chatPartnerEmail;
    private MessageAdapter messageAdapter;
    private String profileImage = "";
    private String profession = "";
    private String location = "";
    private String userName = "";
    private String safeuid = "";
    private int age = 0;
    private String languages = "";
    private String availability = "";
    private String dob;

    public Chat_Fragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_chat_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Retrieve views from layout
        LinearLayout linearLayout1 = view.findViewById(R.id.profilecontainer);
        ImageButton professionalImage = view.findViewById(R.id.professionalImage);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        tvproffessionalname = view.findViewById(R.id.proffesionalName);
        Button btnAddDate = view.findViewById(R.id.btnAddDate);

        // Set up "Add date to calendar" button click with runtime permission check:
        btnAddDate.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
                        PERMISSION_REQUEST_CODE);
            } else {
                showDatePickerForCalendarEvent();
            }
        });



        // Get current user info from FirebaseAuth.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserEmail = formatEmail(currentUser.getEmail().toLowerCase());
            currentUserName = currentUser.getDisplayName();
            if (currentUserName == null || currentUserName.isEmpty()) {
                currentUserName = currentUserEmail; // fallback
            }
            Log.d("ChatFragment", "Current user: " + currentUserName);
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve chat partner data from bundle arguments.
        Bundle args = getArguments();
        if (args != null) {
            profileImage = args.getString("chatPartnerImage", "");
            safeuid = args.getString("chatPartnerUid", "");
            chatPartnerEmail = formatEmail(args.getString("chatPartnerUid", ""));
            profession = args.getString("profession", "");
            location = args.getString("location", "");
            userName = args.getString("userName", "");
            age = args.getInt("age", 0);
            dob = args.getString("dob", "");
            languages = args.getString("languages", "");
            availability = args.getString("availability", "");
            Log.d("ChatFragment", "Chat partner email: " + chatPartnerEmail);
        } else {
            Toast.makeText(getContext(), "Chat partner info missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delay image loading until layout is ready.
        view.post(() -> {
            if (professionalImage != null) {
                try {
                    tvproffessionalname.setText(userName);
                    byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Glide.with(linearLayout1)
                            .load(decodedByte)
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.default_profile)
                            .into(professionalImage);
                } catch (Exception e) {
                    Glide.with(linearLayout1)
                            .load(profileImage)
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.default_profile)
                            .into(professionalImage);
                }
            } else if (professionalImage != null) {
                Glide.with(linearLayout1)
                        .load(R.drawable.default_profile)
                        .transform(new CircleCrop())
                        .into(professionalImage);
            } else {
                Log.e("ChatFragment", "professionalImage is null");
            }
        });

        // Click listener on professionalImage to navigate to searchProfile.
        if (professionalImage != null) {
            professionalImage.setOnClickListener(v -> {
                searchProfile searchProfileFragment = new searchProfile();
                Bundle bundle = new Bundle();
                bundle.putString("uid", safeuid);
                bundle.putString("profileImage", profileImage);
                bundle.putString("profession", profession);
                bundle.putString("location", location);
                bundle.putString("userName", userName);
                bundle.putString("dob", dob);
                bundle.putInt("age", age);
                bundle.putString("languages", languages);
                bundle.putString("availability", availability);
                searchProfileFragment.setArguments(bundle);
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_layout, searchProfileFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // Generate chat ID.
        if (!TextUtils.isEmpty(currentUserEmail) && !TextUtils.isEmpty(chatPartnerEmail)) {
            chatId = currentUserEmail.compareTo(chatPartnerEmail) < 0
                    ? currentUserEmail + "_" + chatPartnerEmail
                    : chatPartnerEmail + "_" + currentUserEmail;
            Log.d("ChatFragment", "Generated chatId: " + chatId);
        } else {
            Toast.makeText(getContext(), "Chat ID generation failed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize MessageAdapter and load messages.
        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserName);
        messagesRecyclerView.setAdapter(messageAdapter);
        createChatIfNotExists();
        loadChatMessages();

        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText);
                etMessage.setText("");
            }
        });
    }

    // Request permission result callback.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length >= 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showDatePickerForCalendarEvent();
            } else {
                Toast.makeText(getContext(), "Calendar permissions are required", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    // Method to start date picker dialog.
    private void showDatePickerForCalendarEvent() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (dateView, year, month, dayOfMonth) -> {
                    // Inflate custom dialog with TimePicker and EditText.
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    View dialogView = inflater.inflate(R.layout.dialog_time_with_text, null);
                    TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
                    EditText etEventText = dialogView.findViewById(R.id.etEventText);
                    timePicker.setIs24HourView(true);

                    new AlertDialog.Builder(getContext())
                            .setTitle("Select time & enter event text")
                            .setView(dialogView)
                            .setPositiveButton("OK", (dialogInterface, whichButton) -> {
                                int hourOfDay, minute;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    hourOfDay = timePicker.getHour();
                                    minute = timePicker.getMinute();
                                } else {
                                    hourOfDay = timePicker.getCurrentHour();
                                    minute = timePicker.getCurrentMinute();
                                }
                                String eventText = etEventText.getText().toString().trim();

                                Calendar chosenCalendar = Calendar.getInstance();
                                chosenCalendar.set(Calendar.YEAR, year);
                                chosenCalendar.set(Calendar.MONTH, month);
                                chosenCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                chosenCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                chosenCalendar.set(Calendar.MINUTE, minute);
                                chosenCalendar.set(Calendar.SECOND, 0);

                                addEventToCalendar(chosenCalendar, eventText);
                            })
                            .setNegativeButton("Cancel", null)
                            .create().show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private String formatEmail(String email) {
        return email.replace("@", "_").replace(".", "_");
    }

    private void loadChatMessages() {
        Query messagesQuery = db.collection("chats").document(chatId)
                .collection("messages").orderBy("timestamp", Query.Direction.ASCENDING);
        messagesQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("ChatFragment", "Error loading messages", error);
                return;
            }
            if (value != null && !value.isEmpty()) {
                List<MessageModel> messagesList = new ArrayList<>();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    MessageModel message = doc.toObject(MessageModel.class);
                    messagesList.add(message);
                }
                if (messageAdapter != null) {
                    messageAdapter.updateMessages(messagesList);
                    Log.d("ChatFragment", "Messages updated. Count: " + messagesList.size());
                    messagesRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
                }
            } else {
                Log.d("ChatFragment", "No messages found");
            }
        });
    }

    private void addEventToCalendar(Calendar startTime, String eventText) {
        String title = !TextUtils.isEmpty(eventText) ? eventText : "Chat Appointment";
        String description = "Chat appointment with " + userName;

        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 1); // 1-hour duration

        ContentResolver cr = requireContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // Adjust if necessary.
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        try {
            Uri eventUri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (eventUri != null) {
                long eventID = Long.parseLong(eventUri.getLastPathSegment());

                ContentValues reminderValues = new ContentValues();
                reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
                reminderValues.put(CalendarContract.Reminders.MINUTES, 60); // Reminder 1 hour before
                reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

                Toast.makeText(requireContext(), "Event added to calendar", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to add event", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String text) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("sender", currentUserName);
        messageData.put("text", text);
        messageData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    // Message sent; snapshot listener will update the UI.
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.update("LastMessage", text);
        chatRef.update("LastMessageTimestamp", FieldValue.serverTimestamp());
    }

    private void createChatIfNotExists() {
        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("user1", currentUserEmail);
                    chatData.put("user2", chatPartnerEmail);
                    chatData.put("createdAt", FieldValue.serverTimestamp());
                    chatData.put("LastMessage", "No messages yet");
                    chatData.put("LastMessageTimestamp", "No timestamp yet");

                    chatRef.set(chatData).addOnSuccessListener(aVoid -> {
                        Log.d("ChatFragment", "Chat created successfully");
                        new Handler(Looper.getMainLooper()).postDelayed(this::loadChatMessages, 100);
                    }).addOnFailureListener(e ->
                            Log.e("ChatFragment", "Failed to create chat", e)
                    );
                } else {
                    Log.d("ChatFragment", "Chat already exists");
                    new Handler(Looper.getMainLooper()).postDelayed(this::loadChatMessages, 100);
                }
            } else {
                Log.e("ChatFragment", "Error checking chat existence", task.getException());
            }
        });
    }
}
