package onespot.pivotal.api.dao;

import gumi.builders.url.UrlParameterMultimap;
import onespot.pivotal.rest.JsonRestClient;

/**
 * Created by ian on 3/29/15.
 */
public abstract class DAO {
    protected final JsonRestClient jsonRestClient;
    protected final String path;
    protected UrlParameterMultimap params;

    // TODO(ic): DAOs should be immutable, params currently isn't
    //           Consider making params a PMap
    //           (see http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/PMap.html)
    public DAO(JsonRestClient jsonRestClient, String path, UrlParameterMultimap params) {
        this.jsonRestClient = jsonRestClient;
        this.path = path;
        this.params = params;
    }
}