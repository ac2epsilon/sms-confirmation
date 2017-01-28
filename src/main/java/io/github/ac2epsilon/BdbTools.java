package io.github.ac2epsilon;

import com.sleepycat.je.*;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.je.DatabaseException;

import java.io.File;
import java.util.Iterator;

/**
 * Created by ac2 on 23.01.17.
 */
public class BdbTools {
    EnvironmentConfig envCfg;
    StoreConfig storeCfg;
    WriteOptions wo;
    Environment env;
    EntityStore store;
    PrimaryIndex<String,Confirmation> idx;

    /**
     * Constructs BerkeleyDB objects to store Confirmation entities
     *
     * @return Object to store and retrieve Confirmation entities
     */
    BdbTools() {
        envCfg = new EnvironmentConfig();
        envCfg.setAllowCreate(true);
        envCfg.setTransactional(true);

        storeCfg = new StoreConfig();
        storeCfg.setAllowCreate(true);
        storeCfg.setTransactional(true);

        wo = new WriteOptions();
        try {
            File dataDir = new File("./dbEnv");
            if (!dataDir.exists()) dataDir.mkdir();
            env = new Environment(dataDir, envCfg);
            store = new EntityStore(env, "EntityStore", storeCfg);
            idx = store.getPrimaryIndex(String.class, Confirmation.class);
        } catch (DatabaseException dbe) { dbe.printStackTrace(); }
    }

    /**
     *
     * Adds new or overwrites Confirmation entity to database. Phone number serves as
     * primary key, so it will unique, and DB will not hold more then one
     *
     * @param phone Phone number, associated with confirmation request
     * @param code 4-digit code, sent to phone, mentioned above
     * @return Confirmation entity, stored in DB
     */
    Confirmation add(String phone, String code) {
        Confirmation confirmation = new Confirmation(phone, code);
        idx.put(null, confirmation, Put.OVERWRITE, wo.setTTL(1)); /* one day */
        return confirmation;
    }

    /**
     * Drops TTL flag from Confirmation entity in DB
     *
     * @param confirmation Confirmation object will be persisted removing TTL
     */
    void putNoTTL(Confirmation confirmation) {
        idx.put(null, confirmation, Put.OVERWRITE, wo.setTTL(0).setUpdateTTL(true));
    }

    /**
     * Returns Confirmation by UserId
     *
     * @param userId Returns Confirmation from DB by UserId
     * @return Asked Confirmation or null, if given key is not exist in DB
     */
    Confirmation get(String userId)  {
        return idx.get(userId);
    }

    /**
     * Closes BerkekeyDB (effectivelly stopping working threads)
     */
    void close() {
        if (store!=null) store.close();
        if (env!=null) env.close();
    }

    /**
     * Deletes Confirmation record from DB
     *
     * @param userId User (given as phone number) to delete
     * @return Confirmation of removed record (not persisted any more)
     */
    Confirmation delete(String userId) {
        Confirmation result = get(userId);
        if (result!=null) idx.delete(userId);
        return result;

    }
    /**
     * Iterates over DB and calls
     *
     * @param callback Functional snippet to call for every Confirmation in DB
     */
    void iterate(ConfirmationLambda callback) {
        EntityCursor<Confirmation> pi_cursor = idx.entities();
        try {
            Iterator<Confirmation> it = pi_cursor.iterator();
            while (it.hasNext()) callback.run(it.next());
        } finally { pi_cursor.close(); }
    }
}

/**
 * Supporting interface to make functional calls in iterate()
 */
interface ConfirmationLambda {
    void run(Confirmation confirmation);
}
