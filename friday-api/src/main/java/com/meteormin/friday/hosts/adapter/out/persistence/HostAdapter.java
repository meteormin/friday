package com.meteormin.friday.hosts.adapter.out.persistence;

import com.meteormin.friday.hexagon.annotation.PersistenceAdapter;
import com.meteormin.friday.hosts.adapter.out.persistence.mapper.HostMapper;
import com.meteormin.friday.hosts.application.exception.NotFoundHostException;
import com.meteormin.friday.hosts.application.port.out.HostPort;
import com.meteormin.friday.hosts.domain.Host;
import com.meteormin.friday.hosts.domain.HostFilter;
import com.meteormin.friday.hosts.domain.WhereHost;
import com.meteormin.friday.hosts.domain.WherePublish;
import com.meteormin.friday.infrastructure.persistence.repositories.HostEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class HostAdapter implements HostPort {
    private final HostEntityRepository hostRepository;
    private final HostMapper hostMapper;

    @Override
    public Host create(Host host) {
        var entity = hostMapper.createHostEntity(host);
        return hostMapper.toHostDomain(
                hostRepository.save(entity));
    }

    @Override
    public boolean isUniqueHost(WhereHost whereHost) {
        return hostRepository.existsByHostAndUserId(whereHost.host(), whereHost.userId());
    }

    @Override
    public void deleteById(Long id) {
        hostRepository.deleteById(id);
    }

    @Override
    public Optional<Host> findByHost(WhereHost whereHost) {
        return hostRepository.findByHostAndUserId(whereHost.host(), whereHost.userId())
                .map(hostMapper::toHostDomain);
    }

    @Override
    public Page<Host> findByPublish(WherePublish wherePublish, Pageable pageable) {
        return hostRepository.findByPublishAndUserId(
                wherePublish.publish(),
                wherePublish.userId(),
                pageable).map(hostMapper::toHostDomain);
    }

    @Override
    public Page<Host> findAll(HostFilter host) {
        return hostRepository.findHosts(host)
                .map(hostMapper::toHostDomain);
    }

    @Override
    public Optional<Host> findById(Long id) {
        return hostRepository.findById(id)
                .map(hostMapper::toHostDomain);
    }

    @Override
    public Host update(Host host) {
        var entity = hostRepository.findById(host.getId())
                .orElseThrow(NotFoundHostException::new);
        return hostMapper.toHostDomain(
                hostRepository.save(
                        hostMapper.updateHostEntity(entity, host)));
    }

}
