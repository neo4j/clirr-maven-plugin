package org.codehaus.mojo.clirr;

import java.util.Set;

import net.sf.clirr.core.ApiDifference;

public class MessageCodeFilter implements ApiDifferenceFilter
{
    public interface Codes
    {
        public static final Integer CLASS_INCREASED_VISIBILITY = 1000;
        public static final Integer CLASS_DECREASED_VISIBILITY = 1001;
        
        public static final Integer FIELD_ADDED = 6000;
        public static final Integer FIELD_REMOVED = 6001;
        public static final Integer FIELD_INCREASED_VISIBILITY = 6009;
        public static final Integer FIELD_DECREASED_VISIBILITY = 6010;
        
        public static final Integer METHOD_REMOVED = 7002;
        public static final Integer METHOD_DECREASED_VISIBILITY = 7009;
        public static final Integer METHOD_INCREASED_VISIBILITY = 7010;
        public static final Integer METHOD_ADDED_TO_INTERFACE = 7012;
        
        public static final Integer ABSTRACT_METHOD_ADDED = 7013;
        
        public static final Integer CLASS_ADDED = 8000;
        public static final Integer CLASS_REMOVED = 8001;
        
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
        
        if( includes.size() == 0 || includes.contains(diff.getMessage().getId()) )
        {
            return true;
        }
        
        return false;
    }

}
