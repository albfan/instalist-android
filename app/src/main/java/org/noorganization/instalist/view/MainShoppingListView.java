package org.noorganization.instalist.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.noorganization.instalist.GlobalApplication;
import org.noorganization.instalist.R;
import org.noorganization.instalist.controller.implementation.ControllerFactory;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.touchlistener.IOnShoppingListClickListenerEvents;
import org.noorganization.instalist.touchlistener.sidebar.OnShoppingListAddClickListener;
import org.noorganization.instalist.view.activity.SettingsActivity;
import org.noorganization.instalist.view.event.ToolbarChangeMessage;
import org.noorganization.instalist.view.fragment.ShoppingListOverviewFragment;
import org.noorganization.instalist.view.interfaces.IBaseActivity;
import org.noorganization.instalist.view.interfaces.IBaseActivityListEvent;
import org.noorganization.instalist.view.interfaces.IFragment;
import org.noorganization.instalist.view.interfaces.ISideDrawerListDataEvents;
import org.noorganization.instalist.view.middleware.implementation.SideDrawerListManager;
import org.noorganization.instalist.view.utils.PreferencesManager;
import org.noorganization.instalist.view.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * MainShoppingListView handles the display of an selected ShoppingList, so that the corresponding
 * items of this list are shown to the user.
 * <p/>
 * Is dependant on the selected list.
 *
 * @author TS
 */
public class MainShoppingListView extends AppCompatActivity implements IBaseActivity, IBaseActivityListEvent, IOnShoppingListClickListenerEvents, ISideDrawerListDataEvents {

    private final static String LOG_TAG               = MainShoppingListView.class.getName();
    private final static String DEFAULT_CATEGORY_NAME = "(default)";

    public final static String KEY_LISTNAME          = "list_name";
    public final static String KEY_LISTID            = "list_id";
    public static final String SIDE_DRAWER_TRANSLATE = "side_drawer_translate";

    private Toolbar mToolbar;

    private FrameLayout    mFrameLayout;
    private RelativeLayout mLeftMenuDrawerRelativeLayout;
    private EditText       mNewNameEditText;

    private Button mAddListButton;
    private Button mAddCategoryButton;

    /**
     * For creation of an icon at the toolbar to toggle the navbar.
     */
    private ActionBarDrawerToggle mNavBarToggle;

    /**
     * Layout reference of the side drawer navbar.
     */
    private DrawerLayout mDrawerLayout;

    private Button mSettingsButton;

    /**
     * Title of the toolbar.
     */
    private String mTitle;

    /**
     * Name of the current list
     */
    private String mCurrentListName;

    private long mDefaultCategoryId;

    private SideDrawerListManager mDrawerListManager;

    private List<IFragment> mFragments;
    private float mLastDrawerTranslate = 0.0f;

    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mActivity = this;
        mFragments = new ArrayList<>(1);
        setContentView(R.layout.activity_main_shopping_list_view);

        ExpandableListView expandableListView;
        ListView           plainListView;

        // init PreferencesManager
        PreferencesManager.initializeInstance(this);
        mDefaultCategoryId = PreferencesManager.getInstance().getLongValue(PreferencesManager.KEY_DEFAULT_CATEGORY_ID);
        Category category = Category.findById(Category.class, mDefaultCategoryId);

        if (category == null) {
            // if not set generate a standard category and set the id of it to preferences
            mDefaultCategoryId = ControllerFactory.getCategoryController().createCategory(DEFAULT_CATEGORY_NAME).getId();
            PreferencesManager.getInstance().setValue(PreferencesManager.KEY_DEFAULT_CATEGORY_ID, mDefaultCategoryId);
        }

        // init and setup toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout_container);
        expandableListView = (ExpandableListView) findViewById(R.id.drawer_layout_custom_list_name_view);

        plainListView = (ListView) findViewById(R.id.drawer_layout_custom_plain_shopping_list_overview);
        mNewNameEditText = (EditText) findViewById(R.id.drawer_layout_custom_new_name);

        mAddListButton = (Button) findViewById(R.id.drawer_layout_custom_create_list);
        mAddCategoryButton = (Button) findViewById(R.id.drawer_layout_custom_create_category);

        mLeftMenuDrawerRelativeLayout = (RelativeLayout) findViewById(R.id.list_view_left_side_navigation);
        mSettingsButton = (Button) findViewById(R.id.drawer_layout_custom_settings);
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mDrawerListManager = new SideDrawerListManager(this, plainListView, expandableListView);

        mDrawerLayout.setFitsSystemWindows(true);
        assignDrawer();
        if (savedInstanceState == null) {
            if (ShoppingList.count(ShoppingList.class, null, new String[]{}) > 0) {
                selectList(ShoppingList.listAll(ShoppingList.class).get(0));
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu _Menu, View _View, ContextMenu.ContextMenuInfo _MenuInfo) {
        Log.i(LOG_TAG, "Context menu called.");
        _Menu = mDrawerListManager.createContextMenu(_Menu, _View, _MenuInfo);
        super.onCreateContextMenu(_Menu, _View, _MenuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem _Item) {
        Log.i(LOG_TAG, "Context menu item selected.");
        mDrawerListManager.onContextMenuItemClicked(_Item);
        return super.onContextItemSelected(_Item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        //mToolbar.inflateMenu(R.menu.menu_toolbar_main_listview);
        // check if the drawer is open

        return true;
    }

    public void onEventMainThread(ToolbarChangeMessage _message) {
        if (_message.mNewLockState != null) {
            Log.d("recv", "lock");
            setDrawerLockMode(_message.mNewLockState ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                    DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        if (_message.mNewTitle != null) {
            Log.d("recv", "title");
            setToolbarTitle(_message.mNewTitle);
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLeftMenuDrawerRelativeLayout);
        if (drawerOpen) {
            menu.clear();
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // The action bar home/up action should open or close the navbar.
        // ActionBarDrawerToggle will take care of this.
        // Consume the the onOptionsItemSelected event.
        if (mNavBarToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mNavBarToggle.syncState();
    }


    @Override
    protected void onResume() {
        super.onResume();
        ((ChangeHandler) GlobalApplication.getChangeHandler()).setCurrentBaseActivity(this);
        mAddListButton.setOnClickListener(new OnShoppingListAddClickListener(mDefaultCategoryId, mNewNameEditText));

        mAddCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = ViewUtils.validateAndGetResultEditText(v.getContext(), mNewNameEditText);
                if (categoryName == null) {
                    return;
                }

                // create new category if insert of category failed, there will be shown an
                // error hint to the user.
                Category category = ControllerFactory.getCategoryController().createCategory(categoryName);
                if (category == null) {
                    mNewNameEditText.setError(getResources().getString(R.string.category_exists));
                    return;
                } else {
                    addCategory(category);
                    mAddCategoryButton.setVisibility(View.GONE);
                    ViewUtils.removeSoftKeyBoard(v.getContext(), mNewNameEditText);
                    mNewNameEditText.setText("");
                    mNewNameEditText.clearFocus();
                }
            }
        });

        mNewNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View _View, boolean _HasFocus) {
                if (_HasFocus) {
                    ((EditText) _View).setError(null);
                    mAddCategoryButton.setVisibility(View.VISIBLE);
                    mAddListButton.setVisibility(View.VISIBLE);
                } else {
                    mAddCategoryButton.setVisibility(View.GONE);
                    mAddListButton.setVisibility(View.GONE);
                    ViewUtils.removeSoftKeyBoard(_View.getContext(), mNewNameEditText);
                }
            }
        });
        mNewNameEditText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ((EditText) v).setError(null);
                return false;
            }
        });

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent;
                settingsIntent = new Intent(mActivity, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        EventBus.getDefault().register(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        ((ChangeHandler) GlobalApplication.getChangeHandler()).setCurrentBaseActivity(null);
        mAddListButton.setOnClickListener(null);
        mNewNameEditText.setOnKeyListener(null);
        mSettingsButton.setOnClickListener(null);

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mNavBarToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            // create a leave message box to prevent accidentially exit the app.
            new AlertDialog.Builder(this)
                    .setTitle("Exit App?")
                    .setMessage("Are you sure you want to leave this georgeous app?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            exitApp();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    public void exitApp() {
        super.onBackPressed();
    }

    // --------------------------------------------------------------------------------
    // own public methods
    // --------------------------------------------------------------------------------


    @Override
    public void selectList(ShoppingList _ShoppingList) {
        if (_ShoppingList.mName == null) {
            throw new NullPointerException("Name of ShoppingList must be set.");
        }


        // always close the drawer
        mDrawerLayout.closeDrawer(mLeftMenuDrawerRelativeLayout);

        // list is the same as the current one
        // no need to do then something
        if (_ShoppingList.mName.equals(mCurrentListName)) {
            return;
        }

        // decl
        Bundle   args;
        Fragment fragment;

        // init
        mCurrentListName = _ShoppingList.mName;
        fragment = ShoppingListOverviewFragment.newInstance(_ShoppingList.getId());
        ViewUtils.addFragment(this, fragment);
    }

    @Override
    public boolean onKeyUp(int _KeyCode, KeyEvent _Event) {
        if(_KeyCode == KeyEvent.KEYCODE_BACK){
            if(mNewNameEditText.hasFocus()){
                mNewNameEditText.clearFocus();
                return true;
            } else if (mDrawerLayout.isDrawerOpen(mLeftMenuDrawerRelativeLayout)){
                mDrawerLayout.closeDrawers();
                return true;
            }
        }
        return super.onKeyUp(_KeyCode, _Event);
    }

    @Override
    public void setDrawerLockMode(int _DrawerLayoutMode) {
        mDrawerLayout.setDrawerLockMode(_DrawerLayoutMode);
    }

    /**
     * Changes from the current fragment to the given fragment.
     * Adds the current fragment to the backstack.
     *
     * @param _Fragment the fragment that should be created.
     */
    @Override
    public void changeFragment(Fragment _Fragment) {
        Log.w("NoOrg Deprecated", "MainShoppingListView's changeFragment.");
        ViewUtils.addFragment(this, _Fragment);
    }

    @Override
    public void setNavigationIcon(int _ResId) {
        mToolbar.setNavigationIcon(_ResId);
    }

    @Override
    public void setNavigationClickListener(View.OnClickListener _ClickListener) {
        mToolbar.setNavigationOnClickListener(_ClickListener);
    }

    @Override
    public void bindDrawerLayout() {
        assignDrawer();
    }

    /**
     * Sets the text of the toolbar title, when activity is updated.
     *
     * @param _Title, the title of the toolbar
     */
    @Override
    public void setToolbarTitle(String _Title) {
        mTitle = _Title;
        mToolbar.setTitle(mTitle);
    }

    @Override
    public void addCategory(Category _Category) {
        mDrawerListManager.addCategory(_Category);
    }

    @Override
    public void updateCategory(Category _Category) {
        mDrawerListManager.updateCategory(_Category);
    }

    @Override
    public void removeCategory(Category _Category) {
        Category           category      = Category.findById(Category.class, mDefaultCategoryId);
        List<ShoppingList> shoppingLists = Category.getListsWithoutCategory();
        for (ShoppingList shoppingList : shoppingLists) {
            ShoppingList shoppingList1 = ControllerFactory.getListController().moveToCategory(shoppingList, category);
            updateList(shoppingList1);
        }
        mDrawerListManager.removeCategory(_Category);
    }

    @Override
    public void addList(ShoppingList _ShoppingList) {
        mDrawerListManager.addList(_ShoppingList);
    }

    @Override
    public void updateList(ShoppingList _ShoppingList) {
        mDrawerListManager.updateList(_ShoppingList);
    }

    @Override
    public void removeList(ShoppingList _ShoppingList) {
        mDrawerListManager.removeList(_ShoppingList);
        notifyFragmentsShoppingListRemoved(_ShoppingList);
    }

    @Override
    public void setSideDrawerAddListButtonListener(long _CategoryId) {
        mAddListButton.setOnClickListener(new OnShoppingListAddClickListener(_CategoryId, mNewNameEditText));
    }

    @Override
    public void onShoppingListClicked(ShoppingList _ShoppingList) {
        selectList(_ShoppingList);
    }

    /**
     * Set a drawer to a fragmentlayout.
     */
    public void assignDrawer() {
        //mToolbar.setNavigationIcon(R.mipmap.ic_menu_white_36dp);
        // navbar custom design of toolbar
        mNavBarToggle = new ActionBarDrawerToggle(
                this,                       // host activity
                mDrawerLayout,              // DrawerLayout instance
                mToolbar,                   // Reference to toolbar
                R.string.nav_drawer_open,   // text for drawer on open icon
                R.string.nav_drawer_close   // text for drawer on close icon
        ) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // mToolbar.setNavigationIcon(R.mipmap.ic_menu_white_36dp);
                mToolbar.setTitle(mTitle);
                // check if options menu has changed
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //mToolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white_36dp);
                String tmpTitle = getString(R.string.choose_list);
                mToolbar.setTitle(tmpTitle);
                // check if options menu has changed
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View _DrawerView, float _SlideOffset) {
                super.onDrawerSlide(_DrawerView, _SlideOffset);
                float moveFactor = (mDrawerLayout.getWidth() * _SlideOffset);
                slideDrawer(moveFactor);
            }
        };
        mDrawerLayout.setDrawerListener(mNavBarToggle);
    }

    private void slideDrawer(float _MoveFactor){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            mFrameLayout.setTranslationX(_MoveFactor);
        }
        else
        {
            TranslateAnimation anim = new TranslateAnimation(mLastDrawerTranslate, _MoveFactor, 0.0f, 0.0f);
            anim.setDuration(0);
            anim.setFillAfter(true);
            mFrameLayout.startAnimation(anim);
            mLastDrawerTranslate = _MoveFactor;
        }
    }

    @Override
    public void registerFragment(Fragment fragment) {
        try {
            mFragments.add((IFragment)fragment);
        }catch(ClassCastException e){
            throw new  ClassCastException(fragment.getClass().toString() + " has no IFragment implemented");
        }
    }

    @Override
    public void unregisterFragment(Fragment fragment) {
        mFragments.remove((IFragment)fragment);
    }

    /**
     * Notifies registered Fragments that an ShoppingList was removed.
     * @param _ShoppingList the shopping list that was removed.
     */
    public void notifyFragmentsShoppingListRemoved(ShoppingList _ShoppingList){
        for(IFragment fragment : mFragments){
           fragment.onShoppingListRemoved(_ShoppingList);
        }
    }
}
