package com.meteormin.friday.hosts.adapter.out.persistence;

import com.meteormin.friday.hexagon.annotation.PersistenceAdapter;
import com.meteormin.friday.hosts.adapter.out.persistence.mapper.SearchMapper;
import com.meteormin.friday.hosts.application.exception.NotFoundHostException;
import com.meteormin.friday.hosts.application.port.out.SearchPort;
import com.meteormin.friday.hosts.domain.searches.Search;
import com.meteormin.friday.hosts.domain.searches.SearchFilter;
import com.meteormin.friday.hosts.domain.searches.WhereSearch;
import com.meteormin.friday.infrastructure.persistence.repositories.SearchEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SearchAdapter implements SearchPort {
    private final SearchEntityRepository searchRepository;
    private final SearchMapper searchMapper;

    @Override
    public Search createSearch(Search search) {
        var entity = searchMapper.createSearchEntity(search);
        return searchMapper.toSearchDomain(
                searchRepository.save(entity));
    }

    @Override
    public boolean isUniqueSearch(WhereSearch whereSearch) {
        return searchRepository.existsByHostIdAndQueryKeyAndQuery(
                whereSearch.hostId(),
                whereSearch.queryKey(),
                whereSearch.query());
    }

    @Override
    public Search updateSearch(Search search) {
        var searchEntity = searchRepository.findById(search.getHostId())
                .orElseThrow(NotFoundHostException::new);

        searchMapper.updateSearchEntity(searchEntity, search);
        return searchMapper.toSearchDomain(
                searchRepository.save(searchEntity));
    }

    @Override
    public Optional<Search> findSearchById(Long id) {
        return searchRepository.findById(id).map(searchMapper::toSearchDomain);
    }

    @Override
    public Page<Search> findSearchAll(SearchFilter searchFilter) {
        return searchRepository.findSearches(searchFilter)
                .map(searchMapper::toSearchDomain);
    }

    @Override
    public void deleteSearchById(Long id) {
        searchRepository.deleteById(id);
    }
}
