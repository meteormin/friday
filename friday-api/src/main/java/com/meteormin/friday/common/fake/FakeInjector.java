package com.meteormin.friday.common.fake;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteormin.friday.common.fake.annotation.NoFake;
import com.meteormin.friday.common.fake.exception.NotAllowedFakeException;
import com.meteormin.friday.common.fake.resolver.ReflectResolver;
import lombok.Getter;
import net.datafaker.Faker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * <p>
 * FakeInjector.
 * </p>
 * <p>
 * Automatically inject fake data
 * </p>
 *
 * @author meteormin
 * @see Faker
 * @since 2023/10/19
 */
public class FakeInjector {

    /**
     * Faker data generator.
     *
     * @see Faker
     */
    @Getter
    private final Faker faker;

    /**
     * object mapper.
     *
     * @see ObjectMapper
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * random generator
     */
    private final Random rand = new Random();

    /**
     * reflect resolver.
     * <p>
     * The default resolver is {@link ReflectResolver}.
     * </p>
     * <p>
     * Generating fake data using Reflection.
     * </p>
     *
     * @see ReflectResolver#resolvePropertyType(Field, Object)
     * @see #inject(Field, Object)
     */
    private final ReflectResolver resolver;

    public FakeInjector(Faker faker, ObjectMapper objectMapper) {
        this.faker = faker;
        this.objectMapper = objectMapper;

        ReflectResolver.Options options = ReflectResolver.Options.builder()
                .array(3)
                .dateTime("1970-01-01 00:00:01", "2038-01-19 03:14:07")
                .number(2, 0, 100)
                .build();

        this.resolver = new ReflectResolver(this, options);
    }

    /**
     * Capabilities available for multiple data injections
     */
    private Function<Object, Object> map;

    /**
     * Generates a list of objects of type T.
     *
     * @param dataClass the class of the objects to generate
     * @param count the number of objects to generate
     * @return a list of generated objects
     * @throws InvocationTargetException if an exception occurs while invoking a method
     * @throws NoSuchMethodException if a specified method does not exist
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible
     */
    public <T> List<T> generate(Class<T> dataClass, int count)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            T instance = generate(dataClass);
            list.add(instance);
        }

        return list;
    }

    /**
     * Generate a list of objects of type T.
     *
     * @param dataClass the class of the objects to generate
     * @param count the number of objects to generate
     * @param map a function to map the generated objects
     * @return a list of generated objects
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws NoSuchMethodException if the specified method cannot be found
     * @throws InstantiationException if a new instance of the class cannot be instantiated
     * @throws IllegalAccessException if the underlying method is inaccessible
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> generate(Class<T> dataClass, int count, UnaryOperator<T> map)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        this.map = (Function<Object, Object>) map;
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            T instance = generate(dataClass);
            list.add(instance);
        }

        return list;
    }

    /**
     * Generates a list of objects based on the given count and mapping function.
     *
     * @param count the number of objects to generate
     * @param map the mapping function to generate each object
     * @return the list of generated objects
     * @throws InvocationTargetException if the mapping function throws an exception
     * @throws NoSuchMethodException if the mapping function throws an exception
     * @throws InstantiationException if the mapping function throws an exception
     * @throws IllegalAccessException if the mapping function throws an exception
     */
    public <T> List<T> generate(int count, Function<FakeInjector, T> map)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            T instance = generate(map.apply(this));
            list.add(instance);
        }

        return list;
    }

    /**
     * Generate a new instance of the given object by resolving its fields.
     *
     * @param instance the instance of the object to be generated
     * @param <T> the type of the object
     * @return the generated instance of the object
     * @throws NoSuchMethodException if the method being invoked does not exist
     * @throws InvocationTargetException if an error occurs while invoking the method
     * @throws InstantiationException if an error occurs while instantiating the object
     * @throws IllegalAccessException if there is illegal access to the object or its fields
     */
    @SuppressWarnings("unchecked")
    public <T> T generate(T instance)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        if (instance.getClass().isAnnotationPresent(NoFake.class)) {
            throw new NotAllowedFakeException("No Fake");
        }

        if (instance.getClass().isRecord()) {
            var resolveMap = objectMapper.convertValue(instance, HashMap.class);
            resolve(instance.getClass().getDeclaredFields(), resolveMap);
            return (T) objectMapper.convertValue(resolveMap, instance.getClass());
        }

        resolve(instance.getClass().getDeclaredFields(), instance);

        return instance;
    }

    /**
     * Generates an instance of the specified data class.
     *
     * @param dataClass the class of the data to generate
     * @return an instance of the specified data class
     * @throws NoSuchMethodException if the constructor of the data class cannot be found
     * @throws InvocationTargetException if the constructor of the data class throws an exception
     * @throws InstantiationException if the data class cannot be instantiated
     * @throws IllegalAccessException if the constructor of the data class is not accessible
     */
    public <T> T generate(Class<T> dataClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        Field[] fields = dataClass.getDeclaredFields();

        if (dataClass.isAnnotationPresent(NoFake.class)) {
            // Class has NoFake annotation, throw exception
            throw new NotAllowedFakeException("No Fake");
        }

        try {
            // Try to get constructor and create instance
            Constructor<T> constructor = dataClass.getDeclaredConstructor();
            T instance = constructor.newInstance();
            if (map == null) {
                resolve(fields, instance);
            } else {
                resolve(fields, map.apply(instance));
            }

            return instance;
        } catch (NoSuchMethodException ex) {
            // Can not find constructor or create instance.
            // Inject to `Map Object` and convert to input data class using ObjectMapper
            Map<String, Object> resolveMap = new HashMap<>();
            resolve(fields, resolveMap);
            return objectMapper.convertValue(resolveMap, dataClass);
        }
    }

    /**
     * Generates an object of type T using the provided data class and map function.
     *
     * @param dataClass the class of the object to be generated
     * @param map the function used to map the generated object
     * @param <T> the type of the object to be generated
     * @return the generated object of type T
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws NoSuchMethodException if the requested method cannot be found
     * @throws InstantiationException if a new instance of the object cannot be created
     * @throws IllegalAccessException if access to the requested method is denied
     */
    @SuppressWarnings("unchecked")
    public <T> T generate(Class<T> dataClass, UnaryOperator<T> map)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        this.map = (Function<Object, Object>) map;
        return generate(dataClass);
    }

    /**
     * Generates a value of type T by applying a function to a FakeInjector.
     *
     * @param map the function to apply to the FakeInjector
     * @return the generated value of type T
     * @throws InvocationTargetException if the function throws an exception
     * @throws NoSuchMethodException if the function cannot be found
     * @throws InstantiationException if an instance of T cannot be created
     * @throws IllegalAccessException if access to the function is denied
     */
    public <T> T generate(Function<FakeInjector, T> map)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        return generate(map.apply(this));
    }

    /**
     * Generates a random enum value from the given enum class.
     *
     * @param enumClass the enum class to generate a random value from
     * @return a random enum value
     */
    public <T extends Enum<?>> T randomEnum(Class<T> enumClass) {
        return enumClass.getEnumConstants()[rand.nextInt(
                enumClass.getEnumConstants().length)];
    }

    public <T> T randomList(List<T> ls) {
        return ls.get(rand.nextInt(ls.size()));
    }

    /**
     * Resolves the given array of fields by injecting dependencies into the provided instance.
     *
     * @param fields the array of fields to be resolved
     * @param instance the instance into which the dependencies will be injected
     * @throws IllegalAccessException if access to a field is denied
     * @throws InvocationTargetException if the method being invoked throws an exception
     * @throws NoSuchMethodException if a method is not found
     * @throws InstantiationException if an instance of a class cannot be created
     */
    private void resolve(Field[] fields, Object instance)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            InstantiationException {
        for (Field field : fields) {
            if (!field.isAnnotationPresent(NoFake.class)) {
                inject(field, instance);
            }
        }
    }

    /**
     * Sets the value of a field in an instance using reflection.
     *
     * @param field the field to be set
     * @param instance the instance on which to set the field
     * @throws IllegalAccessException if the field is inaccessible
     * @throws InvocationTargetException if the method or constructor being invoked throws an
     *         exception
     * @throws NoSuchMethodException if the method or constructor being invoked does not exist
     * @throws InstantiationException if the class that declares the underlying field represents an
     *         abstract class, an interface, an array class, a primitive type, or void
     */
    @SuppressWarnings({"java:S3011", "java:S3776"})
    private void inject(Field field, Object instance)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            InstantiationException {
        resolver.resolvePropertyType(field, instance);
    }
}
