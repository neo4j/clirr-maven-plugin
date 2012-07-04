package org.codehaus.mojo.clirr;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import net.sf.clirr.core.ApiDifference;

public class SkipDeprecatedFilter implements ApiDifferenceFilter
{

    private JavaTypeRepository originalClasses;

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
                return clazz.getAnnotation( Deprecated.class ) == null;
            } else if(apiDiff.getAffectedMethod() != null )
            {
                Method method = originalClasses.getMethod( apiDiff.getAffectedClass(), apiDiff.getAffectedMethod() );
                return method.getAnnotation( Deprecated.class ) == null;
            } else 
            {
                // Fields, not supported yet. TODO
                return true;
            }
        } catch(NoSuchElementException e)
        {
            // Ignore things that did not exist previously (they were obviously not deprecated)
            return false;
        }
    }

}
