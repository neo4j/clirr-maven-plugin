package org.codehaus.mojo.clirr;

import java.util.Set;

import net.sf.clirr.core.ApiDifference;

public class MessageCodeFilter implements ApiDifferenceFilter
{
    public interface Codes
    {
        public static final Integer MESSAGE_ADDED_TO_INTERFACE = 7012;
    }

    private Set<Integer> excludes;
    private Set<Integer> includes;

    public MessageCodeFilter(Set<Integer> includes, Set<Integer> excludes)
    {
        this.includes = includes;
        this.excludes = excludes;
    }
    
    public boolean shouldInclude(ApiDifference diff )
    {
        if(excludes.size() > 0 && excludes.contains( diff.getMessage().getId()  ))
        {
            return false;
        }
        
        if(includes.size() == 0 || includes.contains( diff.getMessage().getId() ))
        {
            return true;
        }
        
        return false;
    }

}
