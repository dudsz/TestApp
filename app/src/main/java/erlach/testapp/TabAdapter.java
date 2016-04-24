package erlach.testapp;

import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


/**
 * Created by Mackan on 2016-04-21.
 */
public class TabAdapter extends FragmentStatePagerAdapter {


    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                One tab1 = new One();
                return tab1;
            case 1:
                Two tab2 = new Two();
                return tab2;
            /*
            case 2:
                Three tab3 = new Three();
                return tab3;
            */
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
