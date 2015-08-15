package org.jeecqrs.common.domain.model.multitenancy;

/**
 * Provides the ability to load and store objects with multiple tenants.
 * 
 * @param <T>  the type of the objects
 * @param <OID>  the type to identify the objects
 * @param <TID>  the type to identify the tenants
 * @param <CID>  the type to identify commits
 */
public interface MultiTenancyRepository<T, OID, TID, CID> {

    /**
     * Retrieves the object with the given identity.
     * Must exist.
     * 
     * @param tenantId  the tenantId of the object to retrieve
     * @param id  the id of the object to retrieve
     * @return    the object with the given id
     */
    T ofIdentity(TID tenantId, OID id);

    /**
     * Adds an object to the repository.
     *
     * @param object  the object to add
     * @param commitId  unique id for this commit
     */
    void add(T object, CID commitId);

    /**
     * Saves an instance of the object to the repository.
     * 
     * @param object the object to save
     * @param commitId  unique id for this commit
     */
    void save(T object, CID commitId);
    
}