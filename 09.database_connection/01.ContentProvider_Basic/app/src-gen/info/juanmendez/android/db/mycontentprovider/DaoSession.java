package info.juanmendez.android.db.mycontentprovider;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import info.juanmendez.android.db.mycontentprovider.Country;

import info.juanmendez.android.db.mycontentprovider.CountryDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig countryDaoConfig;

    private final CountryDao countryDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        countryDaoConfig = daoConfigMap.get(CountryDao.class).clone();
        countryDaoConfig.initIdentityScope(type);

        countryDao = new CountryDao(countryDaoConfig, this);

        registerDao(Country.class, countryDao);
    }
    
    public void clear() {
        countryDaoConfig.getIdentityScope().clear();
    }

    public CountryDao getCountryDao() {
        return countryDao;
    }

}
