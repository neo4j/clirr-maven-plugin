package org.codehaus.mojo.clirr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sf.clirr.core.spi.JavaType;

public class JavaTypeRepository
{
    
    private Map<String, Integer> modifierMap = new HashMap<String,Integer>()
    {{
        put( "abstract",     Modifier.ABSTRACT );
        put( "final",        Modifier.FINAL );
        put( "interface",    Modifier.INTERFACE );
        put( "native",       Modifier.NATIVE );
        put( "private",      Modifier.PRIVATE );
        put( "protected",    Modifier.PROTECTED );
        put( "public",       Modifier.PUBLIC );
        put( "static",       Modifier.STATIC );
        put( "strict",       Modifier.STRICT );
        put( "synchronized", Modifier.SYNCHRONIZED );
        put( "transient",    Modifier.TRANSIENT );
        put( "volatile",     Modifier.VOLATILE );
    }};
    
    private Map<String, Class<?>> primitivesMap = new HashMap<String,Class<?>>()
    {{
        put( "void",    void.class );
        put( "int",     int.class );
        put( "long",    long.class );
        put( "float",   float.class );
        put( "double",  double.class );
        put( "boolean", boolean.class );
        put( "byte",    byte.class );
        put( "char",    char.class );
        // Primitive arrays
        put( "int[]",     int[].class );
        put( "long[]",    long[].class );
        put( "float[]",   float[].class );
        put( "double[]",  double[].class );
        put( "boolean[]", boolean[].class );
        put( "byte[]",    byte[].class );
        put( "char[]",    char[].class );
    }};

    private JavaType[] types;
    private ClassLoader classLoader;

    public JavaTypeRepository( JavaType[] javaTypes, ClassLoader classLoader )
    {
        this.types = javaTypes;
        this.classLoader = classLoader;
    }

    public JavaType[] getRawJavaTypes()
    {
        return types;
    }

    public JavaType getClirrType( String className )
    {
        return getRecursiveClirrType( className, types );
    }
    
    private JavaType getRecursiveClirrType( String className, JavaType[]typeSet )
    {
        for(JavaType jt : types)
        {
            if(jt.getName().equals( className ))
            {
                return jt;
            }
            
            return getRecursiveClirrType( className, jt.getInnerClasses() );
        }
        throw new NoSuchElementException("No class named '" + className + "' could be found.");
    }
    
    public Class<?> get(String className)
    {
        try
        {
            if(primitivesMap.containsKey( className ))
            {
                return primitivesMap.get( className );
            }
            
            return classLoader.loadClass( className );
        }
        catch ( ClassNotFoundException e )
        {
            throw new NoSuchElementException("No class named '" + className + "' could be found.");
        }
    }

    // TODO: Refactor the parsing below into a stand-alone class
    public Method getMethod( String clirrClassName, String clirrMethodDefinition )
    {
        // Input:
        // public static org.neo4j.graphdb.Expander addRelationsipFilter(org.neo4j.helpers.Predicate, java.lang.String)
        
        if(clirrMethodDefinition == null)
        {
            throw new NoSuchElementException("No method named <null> in '" + clirrClassName + "' could be found.");
        }
        
        // [..] addRelationsipFilter(org.neo4j.helpers.Predicate, java.lang.String)
        // to
        // [..] addRelationsipFilter(org.neo4j.helpers.Predicate,java.lang.String)
        clirrMethodDefinition = clirrMethodDefinition.replace( ", ", "," );
        
        String[] parts = clirrMethodDefinition.split(" ");
        
        // addRelationsipFilter(org.neo4j.helpers.Predicate)
        String [] nameAndArgs = parts[parts.length-1].split( "\\(" );
        String methodName = nameAndArgs[0];
        
        // org.neo4j.helpers.Predicate,java.lang.String)
        String [] parameterClassNames = nameAndArgs[1].substring( 0, nameAndArgs[1].length()-1 ).split( "," );
        List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
        if(parameterClassNames.length > 0 && parameterClassNames[0].length() > 0) 
        {
            for(String paramClassName : parameterClassNames)
            {
                parameterTypes.add( get(paramClassName) );
            }
        }
        
        // org.neo4j.graphdb.Expander
        Class<?> returnType = get(parts[parts.length-2]);
        
        // public static
        int modifiers = 0x0;
        for(int i=parts.length-3; i>=0 ; i--)
        {
            modifiers |= modifierMap.get( parts[i] );
        }
        
        // Phew.
        
        Class<?> clazz = get( clirrClassName );
        for(Method method : clazz.getDeclaredMethods())
        {
            if(method.getName().equals( methodName ) && 
               (method.getModifiers() & modifiers) == modifiers &&
               method.getReturnType() == returnType &&
               method.getParameterTypes().length == parameterTypes.size())
            {
                
                for(int i=0;i<method.getParameterTypes().length;i++)
                {
                    if(method.getParameterTypes()[i] != parameterTypes.get( i ))
                    {
                        continue;
                    }
                }
                return method;
            }
        }
        throw new NoSuchElementException("No method named '" + methodName + "' in '" + clirrClassName + "' could be found.");
    }

    public Field getField( String className, String fieldName )
    {
        Class<?> clazz = get(className);
        try
        {
            return clazz.getField( fieldName );
        }
        catch ( NoSuchFieldException e )
        {
            throw new NoSuchElementException("No field named '" + fieldName + "' in '" + className + "' could be found.");
        }
        catch ( SecurityException e )
        {
            throw new RuntimeException(e);
        }
    }

}
