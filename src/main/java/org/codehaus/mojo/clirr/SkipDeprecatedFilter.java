package org.codehaus.mojo.clirr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.NoSuchElementException;

import net.sf.clirr.core.ApiDifference;

public class SkipDeprecatedFilter implements ApiDifferenceFilter
{

    private JavaTypeRepository originalClasses;

    public static MessageCodeFilter DEPRECATED_FILTER = new MessageCodeFilter(new HashSet<Integer>(), new HashSet<Integer>(){{
        // Errors that are excluded when they apply to 
        // a deprecated class/field/method
        
        add(MessageCodeFilter.Codes.METHOD_REMOVED);
        add(MessageCodeFilter.Codes.METHOD_DECREASED_VISIBILITY);
        
        add(MessageCodeFilter.Codes.CLASS_REMOVED);
        add(MessageCodeFilter.Codes.CLASS_DECREASED_VISIBILITY);
        
        add(MessageCodeFilter.Codes.FIELD_REMOVED);
        add(MessageCodeFilter.Codes.FIELD_DECREASED_VISIBILITY);
    }});
    
    public SkipDeprecatedFilter( JavaTypeRepository origClasses )
    {
        this.originalClasses = origClasses;
    }

    public boolean shouldInclude( ApiDifference apiDiff )
    {
        try 
        {
            if(apiDiff.getAffectedMethod() == null && apiDiff.getAffectedField() == null)
            {
                Class<?> clazz = originalClasses.get( apiDiff.getAffectedClass() );
                if(clazz.getAnnotation( Deprecated.class ) != null)
                {
                    return DEPRECATED_FILTER.shouldInclude( apiDiff );
                }
            } else if(apiDiff.getAffectedMethod() != null )
            {
                Method method = originalClasses.getMethod( apiDiff.getAffectedClass(), apiDiff.getAffectedMethod() );
                
                if(method.getAnnotation( Deprecated.class ) != null)
                {
                    return DEPRECATED_FILTER.shouldInclude( apiDiff );
                }
            } else 
            {
                Field field = originalClasses.getField( apiDiff.getAffectedClass(), apiDiff.getAffectedField() );
                
                if(field.getAnnotation( Deprecated.class ) != null)
                {
                    return DEPRECATED_FILTER.shouldInclude( apiDiff );
                }
            }
            
            return true;
        } catch(NoSuchElementException e)
        {
            // Include things that did not exist previously (they were obviously not deprecated)
            return true;
        }
    }

}
