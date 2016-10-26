package io.github.agobi.wtfimm.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.util.Date;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.R;
import io.github.agobi.wtfimm.model.Month;
import io.github.agobi.wtfimm.model.Transaction;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, TRListFragment.OnTransactionSelectedListeners {
    private static final String TAG = "MainActivity";
    private Spinner spinner;
    private FireBaseApplication application;
    private FloatingActionButton fab;
    private ActionBarDrawerToggle toggle;


    /* Month spinner in toolbox */
    private static class MyAdapter extends ArrayAdapter<Month> implements ThemedSpinnerAdapter, FireBaseApplication.MonthsChangeListener {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;
        private final LayoutInflater mInflater;
        private final DateFormat formatter;

        MyAdapter(Context context, FireBaseApplication app) {
            super(context, android.R.layout.simple_list_item_1);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
            app.addMonthsChangeListener(this);
            mInflater = LayoutInflater.from(context);
            formatter = app.getSettings().getMonthFormat();
        }

        private String formatDate(long timestamp) {
            return formatter.format(new Date(timestamp*1000));
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            Month m = getItem(position);
            if(m != null)
                textView.setText(formatDate(m.getStart()));
            else
                Log.w(TAG, "Month is null in getView!");

            return view;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            Month m = getItem(position);
            if(m != null)
                ((TextView) view).setText(formatDate(m.getStart()));
            else
                Log.w(TAG, "Month is null in getView!");

            return view;
        }

        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }

        @Override
        public void monthsChanged(Month[] months) {
            clear();
            for(int i=months.length-1; i>=0; --i) {
                add(months[i]);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        application = (FireBaseApplication) getApplication();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        NavigationView navigationView =  (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(new MainActivity.MyAdapter(
                toolbar.getContext(), application));

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if(currentUser == null)
                    startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class));
                else
                    updateUser(currentUser);
            }
        });


        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, OverviewFragment.newInstance())
                    .commit();
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    private void updateUser(FirebaseUser currentUser) {
        Log.d(TAG, "User changed: "+(currentUser!=null?currentUser.getDisplayName():"null"));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header=navigationView.getHeaderView(0);

        if(currentUser != null) {
            ((TextView) header.findViewById(R.id.nameView)).setText(currentUser.getDisplayName());
            ((TextView) header.findViewById(R.id.emailView)).setText(currentUser.getEmail());
            ((ImageView) header.findViewById(R.id.imageView)).setImageURI(currentUser.getPhotoUrl());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "ConnectionResult" + connectionResult);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_accounts) {
            
        } else if (id == R.id.menu_budgets) {

        } else if (id == R.id.menu_categories) {
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        } else if (id == R.id.menu_overview) {
            setupOverviewFragment();
        } else if (id == R.id.menu_transactions) {
            setupTRListFragment();
        } else if (id == R.id.sign_out_button) {
            signOut();
        } else if (id == R.id.disconnect_button) {
            revokeAccess();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupOverviewFragment() {
        showFragment(OverviewFragment.newInstance());
    }

    private void setupTRListFragment() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.

                Month month = (Month) parent.getAdapter().getItem(position);
                showFragment(TRListFragment.newInstance(month));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Month month = (Month) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
        showFragment(TRListFragment.newInstance(month));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTransaction(null);
            }
        });
    }


    private void editTransaction(@Nullable final DataSnapshot data) {
        Transaction tr = data == null?null:data.getValue(Transaction.class);
        final DatabaseReference ref = data == null?application.getTransactions().push():data.getRef();

        TREditDialog tredit = TREditDialog.createDialog(tr);
        tredit.setTransactionSaveListener(new TREditDialog.TransactionSaveListener() {
            @Override
            public void onTREditSave(Transaction transaction) {
                ref.setValue(transaction);
            }
        });

        tredit.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onTransactionSelected(DataSnapshot data) {
        editTransaction(data);
    }

    @Override
    public void setFabVisible(boolean fabVisible) {
        Log.d(TAG, "SetfabVisible "+fabVisible);
        int fabVisibility = fabVisible?View.VISIBLE:View.INVISIBLE;
        if(fab != null) fab.setVisibility(fabVisibility);
    }

    @Override
    public void setSpinnerVisible(boolean spinnerVisible) {
        Log.d(TAG, "SetSPinnerVisible "+spinnerVisible);
        int spinnerVisibility = spinnerVisible?View.VISIBLE:View.INVISIBLE;
        if(spinner != null) spinner.setVisibility(spinnerVisibility);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.content_main, fragment, "Main")
            .addToBackStack(null)
            .commit();
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d(TAG, "InstanceSave");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "Resume");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "Restore");
    }
}
