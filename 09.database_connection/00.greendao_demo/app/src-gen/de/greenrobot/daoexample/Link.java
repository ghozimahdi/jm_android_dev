package de.greenrobot.daoexample;

import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table LINKS.
 */
public class Link {

    private Long id;
    private String url;
    private long countryId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient LinkDao myDao;

    private Country country;
    private Long country__resolvedKey;


    public Link() {
    }

    public Link(Long id) {
        this.id = id;
    }

    public Link(Long id, String url, long countryId) {
        this.id = id;
        this.url = url;
        this.countryId = countryId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getLinkDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCountryId() {
        return countryId;
    }

    public void setCountryId(long countryId) {
        this.countryId = countryId;
    }

    /** To-one relationship, resolved on first access. */
    public Country getCountry() {
        long __key = this.countryId;
        if (country__resolvedKey == null || !country__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CountryDao targetDao = daoSession.getCountryDao();
            Country countryNew = targetDao.load(__key);
            synchronized (this) {
                country = countryNew;
            	country__resolvedKey = __key;
            }
        }
        return country;
    }

    public void setCountry(Country country) {
        if (country == null) {
            throw new DaoException("To-one property 'countryId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.country = country;
            countryId = country.getId();
            country__resolvedKey = countryId;
        }
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
