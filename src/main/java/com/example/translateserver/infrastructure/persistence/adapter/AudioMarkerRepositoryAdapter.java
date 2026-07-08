package com.example.translateserver.infrastructure.persistence.adapter;

import com.example.translateserver.domain.model.AudioMarker;
import com.example.translateserver.domain.repository.AudioMarkerRepository;
import com.example.translateserver.infrastructure.persistence.entity.AudioMarkerEntity;
import com.example.translateserver.infrastructure.persistence.mapper.AudioMarkerMapper;
import com.example.translateserver.infrastructure.persistence.repository.SpringDataAudioMarkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AudioMarkerRepositoryAdapter implements AudioMarkerRepository {

    private final SpringDataAudioMarkerRepository springDataRepository;

    @Override
    public AudioMarker save(AudioMarker marker) {
        AudioMarkerEntity entity = AudioMarkerMapper.toEntity(marker);
        AudioMarkerEntity saved = springDataRepository.save(entity);
        return AudioMarkerMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<AudioMarker> saveAll(List<AudioMarker> markers) {
        List<AudioMarkerEntity> entities = markers.stream()
                .map(AudioMarkerMapper::toEntity)
                .collect(Collectors.toList());
        List<AudioMarkerEntity> saved = springDataRepository.saveAll(entities);
        return saved.stream()
                .map(AudioMarkerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AudioMarker> findByChapterId(Long chapterId) {
        return springDataRepository.findByChapterId(chapterId).stream()
                .map(AudioMarkerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByChapterId(Long chapterId) {
        springDataRepository.deleteByChapterId(chapterId);
    }
}
