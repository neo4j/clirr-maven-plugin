package org.codehaus.mojo.clirr;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sf.clirr.core.ApiDifference;

public class ExternallyInvokedFilter implements ApiDifferenceFilter
{

    public static MessageCodeFilter EXTERNALLY_INVOKED_FILTER = new MessageCodeFilter(new HashSet<Integer>(), new HashSet<Integer>(){{
        // Ignore these errors if interface had an adaptor
        add(MessageCodeFilter.Codes.METHOD_ADDED_TO_INTERFACE);
    }});
    
    private JavaTypeRepository originalClasses;
    private Set<String> annotations;
    private MessageCodeFilter externallyInvokedFilter = EXTERNALLY_INVOKED_FILTER;

    public ExternallyInvokedFilter( Set<String> externallyInvokedAnnotations, JavaTypeRepository origClasses )
    {
        this.annotations = externallyInvokedAnnotations;
        this.originalClasses = origClasses;
    }

    public boolean shouldInclude( ApiDifference apiDiff )
    {
        try {
            if(apiDiff.getAffectedMethod() != null)
            {
                Class<?> clazz = originalClasses.get( apiDiff.getAffectedClass());
                for(Annotation annotation : clazz.getAnnotations())
                {
                    if(annotations.contains( annotation.annotationType().getName() ))
                    {
                        return externallyInvokedFilter.shouldInclude( apiDiff );
                    }
                }
                
                // No adaptor annotation
                return true;
            } else 
            {
                // We only care about method issues, don't filter out methods and fields
                return true;
            }
        } catch(NoSuchElementException e)
        {
            // Ignore things that did not exist previously (they were obviously not deprecated)
            return false;
        }
    }

}
