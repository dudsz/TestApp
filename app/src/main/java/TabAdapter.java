import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import erlach.testapp.One;
import erlach.testapp.Two;

/**
 * Created by Mackan on 2016-04-21.
 */
public class TabAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    public TabAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
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
        return mNumOfTabs;
    }
}
