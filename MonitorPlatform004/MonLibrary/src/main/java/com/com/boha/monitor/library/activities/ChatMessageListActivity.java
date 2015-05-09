package com.com.boha.monitor.library.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ChatDTO;
import com.com.boha.monitor.library.dto.ChatMessageDTO;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.ChatMessageListFragment;
import com.com.boha.monitor.library.util.CacheUtil;

public class ChatMessageListActivity extends ActionBarActivity {

    ChatMessageListFragment chatMessageListFragment;
    ProjectDTO project;
    ChatDTO chat;
    ChatMessageDTO chatMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message_list);
        project = (ProjectDTO)getIntent().getSerializableExtra("project");
        chatMessage = (ChatMessageDTO)getIntent().getSerializableExtra("message");
        chat = (ChatDTO)getIntent().getSerializableExtra("chat");

        chatMessageListFragment = (ChatMessageListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);

        if (chatMessage == null) {
            chatMessageListFragment.setChat(chat);
            chatMessageListFragment.setProject(project);
            return;
        }
        CacheUtil.getCachedData(getApplicationContext(),CacheUtil.CACHE_DATA,new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getCompany() != null) {
                    for (ProjectDTO f: response.getCompany().getProjectList()) {
                        if (chatMessage.getProjectID().intValue() == f.getProjectID().intValue()) {
                            project = f;
                            chatMessageListFragment.setChatID(chatMessage.getChatID());
                            chatMessageListFragment.setProject(project);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    public void refreshMessages() {
        chatMessageListFragment.refreshMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_message_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
