package unipu.oikt.djaranovic.etlak.dijagnoza;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import unipu.oikt.djaranovic.etlak.MainActivity;
import unipu.oikt.djaranovic.etlak.R;
import unipu.oikt.djaranovic.etlak.prijava.PrijavaActivity;
import unipu.oikt.djaranovic.etlak.povijest.PovijestActivity;
import unipu.oikt.djaranovic.etlak.unos.UnosActivity;
import unipu.oikt.djaranovic.etlak.unos.UnosMasaActivity;
import unipu.oikt.djaranovic.etlak.utils.ActivityHelper;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class DijagnozaTabsActivity extends AppCompatActivity { // klasa vezana za zaslon Dijagnoza, kartice

    // privatna varijabla
    private DataHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dijagnoza_tabs);

        helper = new DataHelper(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // dodavanje fragmenata adapteru
        adapter.addFragment(new PieChartFragment(), "Prikaz 1");
        adapter.addFragment(new ScatterChartFragment(), "Prikaz 2");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    // privatna klasa vezana za adapter i fragmente
    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    // navigacija, izbornik kroz aplikaciju
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dijagnoza, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pocetni_menu:
                ActivityHelper.startActivity(DijagnozaTabsActivity.this, MainActivity.class);
                return true;
            case R.id.unos_menu:
                ActivityHelper.startActivity(DijagnozaTabsActivity.this, UnosActivity.class);
                return true;
            case R.id.masa_menu:
                ActivityHelper.startActivity(DijagnozaTabsActivity.this, UnosMasaActivity.class);
                return true;
            case R.id.povijest_menu:
                ActivityHelper.startActivity(DijagnozaTabsActivity.this, PovijestActivity.class);
                return true;
            case R.id.logout_menu:
                helper.logout();
                ActivityHelper.startActivity(DijagnozaTabsActivity.this, PrijavaActivity.class);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}