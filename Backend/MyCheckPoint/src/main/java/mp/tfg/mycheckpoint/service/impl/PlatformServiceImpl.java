package mp.tfg.mycheckpoint.service.impl;
import mp.tfg.mycheckpoint.dto.platform.PlatformDTO;
import mp.tfg.mycheckpoint.mapper.PlatformMapper;
import mp.tfg.mycheckpoint.repository.PlatformRepository;
import mp.tfg.mycheckpoint.service.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PlatformServiceImpl implements PlatformService {
    private final PlatformRepository platformRepository;
    private final PlatformMapper platformMapper;

    @Autowired
    public PlatformServiceImpl(PlatformRepository platformRepository, PlatformMapper platformMapper) {
        this.platformRepository = platformRepository;
        this.platformMapper = platformMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformDTO> getAllPlatforms() {
        return platformMapper.toDtoList(platformRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlatformDTO> getPlatformById(Long id) {
        return platformRepository.findById(id).map(platformMapper::toDto);
    }
}