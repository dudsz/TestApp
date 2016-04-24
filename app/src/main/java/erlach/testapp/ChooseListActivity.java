package erlach.testapp;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class ChooseListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabAdapter tabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setSupportActionBar(toolbar);

        FragmentManager manager = getSupportFragmentManager();
        tabAdapter = new TabAdapter(manager);
        viewPager.setAdapter(tabAdapter);

        //set tablayout with viewpager
        final TabLayout.Tab one = tabLayout.newTab();
        final TabLayout.Tab two = tabLayout.newTab();
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addTab(one.setText("Your lists"), 0);
        tabLayout.addTab(two.setText("Shared lists"), 1);

        tabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.myRed));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }
}
