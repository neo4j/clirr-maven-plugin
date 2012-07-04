package org.codehaus.mojo.clirr;

import net.sf.clirr.core.ApiDifference;

public interface ApiDifferenceFilter
{
    
    public boolean shouldInclude(ApiDifference apiDiff);

}
