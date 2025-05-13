package com.example.proconnect;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Chat_Fragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private FirebaseFirestore db;
    private RecyclerView messagesRecyclerView;
    private EditText etMessage;
    private Button btnSend;
    private TextView tvProfessionalName;
    private String chatId;
    private String currentUserEmail;
    private String currentUserName;
    private String chatPartnerEmail;
    private MessageAdapter messageAdapter;

    // profile data
    private String profileImage = "", profession = "", location = "",
            userName = "", safeuid = "", dob = "";
    private int age = 0;
    private String languages = "", availability = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        LinearLayout profileContainer = view.findViewById(R.id.profilecontainer);
        ImageButton professionalImage = view.findViewById(R.id.professionalImage);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        tvProfessionalName = view.findViewById(R.id.proffesionalName);
        Button btnAddDate = view.findViewById(R.id.btnAddDate);

        // Calendar permission
        btnAddDate.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
                        PERMISSION_REQUEST_CODE);
            } else {
                showDatePickerForCalendarEvent();
            }
        });

        // current user
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu == null) {
            Toast.makeText(getContext(),
                    "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUserEmail = fu.getEmail().toLowerCase()
                .replace("@", "_").replace(".", "_");
        currentUserName = fu.getDisplayName();
        if (TextUtils.isEmpty(currentUserName)) currentUserName = currentUserEmail;

        // partner args
        Bundle args = getArguments();
        if (args == null) {
            Toast.makeText(getContext(),
                    "Chat partner info missing", Toast.LENGTH_SHORT).show();
            return;
        }
        safeuid = args.getString("chatPartnerUid", "");
        chatPartnerEmail = safeuid.toLowerCase()
                .replace("@", "_").replace(".", "_");
        profileImage = args.getString("chatPartnerImage", "");
        userName = args.getString("userName", "");
        profession = args.getString("profession", "");
        location = args.getString("location", "");
        dob = args.getString("dob", "");
        languages = args.getString("languages", "");
        availability = args.getString("availability", "");

        // detect if partner deleted their account
        boolean isUserDeleted = args.getBoolean("isUserDeleted", false);

        // show partner UI
        tvProfessionalName.setText(userName);
        if (!TextUtils.isEmpty(profileImage)) {
            try {
                byte[] decoded = Base64.decode(profileImage, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                Glide.with(profileContainer).load(bmp)
                        .transform(new CircleCrop()).into(professionalImage);
            } catch (Exception e) {
                Glide.with(profileContainer).load(profileImage)
                        .transform(new CircleCrop()).into(professionalImage);
            }
        } else {
            Glide.with(profileContainer).load(R.drawable.default_profile)
                    .transform(new CircleCrop()).into(professionalImage);
        }

        // if deleted, disable messaging
        if (isUserDeleted) {
            professionalImage.setEnabled(false);
            etMessage.setEnabled(false);
            etMessage.setHint("Cannot send messages – user deleted account");
            btnSend.setEnabled(false);
            btnSend.setAlpha(0.5f);
        }

        professionalImage.setOnClickListener(v -> {
            searchProfile sp = new searchProfile();
            Bundle b = new Bundle();
            b.putString("uid", safeuid);
            b.putString("profileImage", profileImage);
            b.putString("profession", profession);
            b.putString("location", location);
            b.putString("userName", userName);
            b.putString("dob", dob);
            b.putString("languages", languages);
            b.putString("availability", availability);
            sp.setArguments(b);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, sp)
                    .addToBackStack(null)
                    .commit();
        });

        // build chatId
        chatId = currentUserEmail.compareTo(chatPartnerEmail) < 0
                ? currentUserEmail + "_" + chatPartnerEmail
                : chatPartnerEmail + "_" + currentUserEmail;

        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserName);
        messagesRecyclerView.setAdapter(messageAdapter);

        loadChatMessages();  // orders by timestamp ascending in the subcollection

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(msg);
                etMessage.setText("");
            }
        });
    }

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

    private void showDatePickerForCalendarEvent() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(getContext(),
                (view, year, month, day) -> {
                    LayoutInflater inf = LayoutInflater.from(getContext());
                    View dialogView = inf.inflate(R.layout.dialog_time_with_text, null);
                    TimePicker tp = dialogView.findViewById(R.id.timePicker);
                    EditText et = dialogView.findViewById(R.id.etEventText);
                    tp.setIs24HourView(true);

                    new AlertDialog.Builder(getContext())
                            .setTitle("Select time & enter event text")
                            .setView(dialogView)
                            .setPositiveButton("OK", (d, w) -> {
                                int h, m;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    h = tp.getHour();
                                    m = tp.getMinute();
                                } else {
                                    h = tp.getCurrentHour();
                                    m = tp.getCurrentMinute();
                                }
                                String text = et.getText().toString().trim();
                                Calendar cal = Calendar.getInstance();
                                cal.set(year, month, day, h, m, 0);
                                addEventToCalendar(cal, text);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void loadChatMessages() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Log.e("ChatFragment", "load msgs failed", err);
                        return;
                    }
                    if (snap != null) {
                        List<MessageModel> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            MessageModel m = doc.toObject(MessageModel.class);
                            if (m != null) list.add(m);
                        }
                        messageAdapter.updateMessages(list);
                        // ← only scroll if there's at least one message
                        if (!list.isEmpty()) {
                            messagesRecyclerView.smoothScrollToPosition(list.size() - 1);
                        }
                    }
                });
    }

    private void addEventToCalendar(Calendar startTime, String eventText) {
        String title = TextUtils.isEmpty(eventText) ? "Chat Appointment" : eventText;
        String desc  = "Chat with " + userName;
        Calendar end = (Calendar) startTime.clone();
        end.add(Calendar.HOUR_OF_DAY, 1);

        ContentResolver cr = requireContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, desc);
        values.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND,   end.getTimeInMillis());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri != null) {
            long eventID = Long.parseLong(uri.getLastPathSegment());
            ContentValues rem = new ContentValues();
            rem.put(CalendarContract.Reminders.EVENT_ID, eventID);
            rem.put(CalendarContract.Reminders.MINUTES, 60);
            rem.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            cr.insert(CalendarContract.Reminders.CONTENT_URI, rem);
            Toast.makeText(getContext(), "Event added to calendar", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to add event", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String text) {
        DocumentReference chatRef = db.collection("chats").document(chatId);

        chatRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(),
                        "Error: "+task.getException(), Toast.LENGTH_SHORT).show();
                return;
            }
            boolean exists = task.getResult().exists();
            Map<String,Object> updates = new HashMap<>();
            updates.put("lastMessage", text);
            updates.put("LastMessageTimestamp", FieldValue.serverTimestamp());

            if (!exists) {
                Map<String,Object> data = new HashMap<>();
                data.put("user1", currentUserEmail);
                data.put("user2", chatPartnerEmail);
                data.put("createdAt", FieldValue.serverTimestamp());
                data.put("participants", Arrays.asList(currentUserEmail, chatPartnerEmail));
                data.putAll(updates);
                chatRef.set(data);
            } else {
                chatRef.update(updates);
            }

            Map<String,Object> msg = new HashMap<>();
            msg.put("sender", currentUserName);
            msg.put("text", text);
            msg.put("timestamp", FieldValue.serverTimestamp());
            chatRef.collection("messages").add(msg);
        });
    }
}
