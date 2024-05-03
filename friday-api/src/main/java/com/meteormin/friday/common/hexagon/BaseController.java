package com.meteormin.friday.common.hexagon;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Base Controller
 *
 * @author meteormin
 * @since 2023/10/19
 */
public abstract class BaseController {

    /**
     * Create URI.
     *
     * <p>
     * 리소스를 정상적으로 생성 했을 때, Created(201)응답을 위해 사용한다.
     * </p>
     * <ol>
     * <li>생성(POST)요청의 경우 응답 코드로 201(CREATED) 상태 값을 반환.</li>
     * <li>SpringBoot는 Location을 지정하여 생성된 리소스를 접근할 수 있는 EndPoint를 제공 하게 끔 설계 됨.</li>
     * <li>ServeltUriComponentBuilder를 통하여 생성된 리소스에 접근 가능한 EndPoint URI를 반환.</li>
     * </ol>
     * <p>
     * example:
     * </p>
     * {@code createUri("/path/{id}", 123)}
     *
     * @param path 생성된 리소스에 접근할 수 있는 EndPoint 경로
     * @param args path를 생성할 때, "{}"(을)를 사용 하여, 템플릿 형식으로 URI를 만들 수 있다.
     * @return Created URI
     */
    protected URI createUri(String path, Object... args) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path(path)
                .buildAndExpand(args)
                .toUri();
    }

    protected URI createUriFromContextPath(String path, Object... args) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path)
                .buildAndExpand(args)
                .toUri();
    }
}
