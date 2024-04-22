package com.meteormin.friday.hosts.application.service;

import com.meteormin.friday.common.error.ForbiddenErrorException;
import com.meteormin.friday.hexagon.annotation.Usecase;
import com.meteormin.friday.hosts.application.exception.ExistsHostException;
import com.meteormin.friday.hosts.application.exception.NotFoundHostException;
import com.meteormin.friday.hosts.application.port.in.query.RetrieveHostQuery;
import com.meteormin.friday.hosts.application.port.in.usecase.HostUsecase;
import com.meteormin.friday.hosts.application.port.out.HostPort;
import com.meteormin.friday.hosts.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Usecase
@RequiredArgsConstructor
public class HostService implements HostUsecase, RetrieveHostQuery {
    /**
     * hostPort.
     */
    private final HostPort hostPort;

    /**
     * Retrieves a host by its ID.
     *
     * @param findHostById the host ID to search for
     * @return the retrieved host
     */
    @Override
    public Host retrieveHostById(HostIds findHostById) {
        var host = hostPort.findById(findHostById.id()).orElseThrow(
                NotFoundHostException::new);

        if (!host.getUserId().equals(findHostById.userId())) {
            throw new ForbiddenErrorException();
        }

        return host;
    }

    /**
     * Retrieves a host based on the specified criteria.
     *
     * @param whereHost the criteria used to retrieve the host
     * @return the retrieved host
     */
    @Override
    public Host retrieveHost(WhereHost whereHost) {
        return hostPort.findByHost(whereHost).orElseThrow(
                NotFoundHostException::new);
    }

    /**
     * Retrieves hosts based on the provided filter.
     *
     * @param filter the filter to apply to the hosts
     * @return the page containing the retrieved hosts
     */
    @Override
    public Page<Host> retrieveHosts(HostFilter filter) {
        if (filter.isEmpty()) {
            throw new ForbiddenErrorException();
        }

        return hostPort.findAll(filter);
    }

    /**
     * Retrieves a page of hosts based on the specified publish filter and pagination settings.
     *
     * @param wherePublish the filter to apply when retrieving hosts
     * @param pageable the pagination settings
     * @return the page of hosts that match the filter and pagination settings
     */
    @Override
    public Page<Host> retrieveHostByPublish(
            WherePublish wherePublish,
            Pageable pageable) {
        return hostPort.findByPublish(wherePublish, pageable);
    }

    /**
     * Creates a new host based on the given CreateHost object.
     *
     * @param host the CreateHost object containing the details of the host to be created
     * @return the newly created Host object
     */
    @Override
    public Host createHost(CreateHost host) {
        WhereHost whereHost = WhereHost.builder()
                .host(host.host())
                .userId(host.userId())
                .build();

        if (!hostPort.isUniqueHost(whereHost)) {
            throw new ExistsHostException();
        }

        return hostPort.create(Host.create(host));
    }

    /**
     * Patch a host.
     *
     * @param patchHost the object containing the information to patch the host
     * @return the updated host after patching
     */
    @Override
    public Host patchHost(PatchHost patchHost) {
        var exists = hostPort.findById(patchHost.ids().id()).orElseThrow(
                NotFoundHostException::new);

        if (!exists.getUserId().equals(patchHost.ids().userId())) {
            throw new ForbiddenErrorException();
        }

        exists.patch(patchHost);

        return hostPort.update(exists);
    }

    /**
     * Deletes a record by its ID if the record belongs to the specified user.
     *
     * @param ids host id and user id
     */
    @Override
    public void deleteHostById(HostIds ids) {
        var deletable = hostPort.findById(ids.id())
                .orElseThrow(NotFoundHostException::new)
                .getUserId().equals(ids.userId());

        if (!deletable) {
            throw new ForbiddenErrorException();
        }

        hostPort.deleteById(ids.id());
    }
}
