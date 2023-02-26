package com.example.snare.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.snare.Entities.Friends;
import com.example.snare.Entities.Group;
import com.example.snare.Entities.WrappingFriends;
import com.example.snare.R;
import com.example.snare.adapters.FriendsAdapter;
import com.example.snare.firebase.FriendsFireBase;
import com.example.snare.firebase.GroupFirebase;
import com.example.snare.firebase.MemberFirebase;
import com.example.snare.listeners.FriendListeners;

import java.util.ArrayList;
import java.util.List;

public class MembersActivity extends AppCompatActivity implements FriendListeners {

    private RecyclerView membersRecycleView;
    private ImageView imageAddMemberMain;
    private GroupLayout popupGroup;
    private List<WrappingFriends> friends = new ArrayList<>();
    private List<String> friendsID = new ArrayList<>();
    private FriendsAdapter friendsAdapter;
    private Group group ;
    private boolean isNew = false;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button delete, cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        group = (Group) getIntent().getSerializableExtra("group");
        setActivity();
    }

    private void setActivity() {
        initializeActivity();
        setListeners();
        getAllMembers();
    }

    private void initializeActivity() {
        imageAddMemberMain = findViewById(R.id.imageAddMemberMain);
        membersRecycleView = findViewById(R.id.membersRecycleView);
    }

    private void setListeners() {
        setImageAddMemberMainListener();
    }

    private void setImageAddMemberMainListener() {
        imageAddMemberMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNew = true;
                addFriend();
            }
        });
    }

    private void addFriend() {

        if(!isNetworkAvailable(getApplicationContext())){
            Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
            return;
        }
        popupGroup = new GroupLayout(MembersActivity.this);
        popupGroup.setDialog(MembersActivity.this);
        Window window = popupGroup.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
        popupGroup.show();
    }

    private void getAllMembers() {

        if(isNetworkAvailable(getApplicationContext())) {
            GroupFirebase groupFirebase = new GroupFirebase();
            groupFirebase.getAllGroups(new GroupFirebase.GroupCallback() {
                @Override
                public void onGroupRetrieved(List<Group> groups) {
                    getMembersByIds(group);
                }

                @Override
                public void onGroupRetrieveError(String error) {

                }
            });

        }else{
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void getMembersByIds(Group group) {
        friendsID = group.getGroupMembers();
        FriendsFireBase friendsFireBase = new FriendsFireBase();
        friendsFireBase.getAllFriends(new FriendsFireBase.GroupCallback() {
            @Override
            public void onGroupRetrieved(List<WrappingFriends> friends) {
                MembersActivity.this.friends = friends;
                filterBasedOnId(MembersActivity.this.friends,friendsID);
                setRecycleView();
            }

            @Override
            public void onGroupRetrieveError(String error) {

            }
        });

    }

    private void filterBasedOnId(List<WrappingFriends> friends, List<String> friendsID) {

        List<WrappingFriends>  temp = new ArrayList<>();
        for(WrappingFriends friend : friends){
            String id = friend.getId();
            if(friendsID.contains(id)){
                temp.add(friend);
            }
        }

        this.friends = temp ;
    }


    public  boolean  isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setRecycleView() {
        membersRecycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        friendsAdapter = new FriendsAdapter(friends,this);
        membersRecycleView.setAdapter(friendsAdapter);
    }

    @Override
    public void onFriendClick(WrappingFriends friend, int position) {

        if(!isNew){

            createDeleteMemberDialog();

        }else{

            isNew = false;
            friendsID.add(0,friend.getId());
            friends.add(0,friend);
            friendsAdapter.notifyItemInserted(0);
            GroupFirebase groupFirebase = new GroupFirebase();
            group.setGroupMembers(friendsID);
            groupFirebase.save(group);
            Toast.makeText(this,"Selected",Toast.LENGTH_SHORT).show();
            popupGroup.dismiss();
        }


    }

    private void createDeleteMemberDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View groupPopUp = getLayoutInflater().inflate(R.layout.member, null);

        cancel = groupPopUp.findViewById(R.id.cancel);
        delete = groupPopUp.findViewById(R.id.delete);

        dialogBuilder.setView(groupPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

        cancel.setOnClickListener(view -> dialog.dismiss());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
               /* if (groups.size() != 0) {
                    groupAdapter.searchGroups(newText);
                }*/

                return false;
            }
        });

        return true;
    }

}