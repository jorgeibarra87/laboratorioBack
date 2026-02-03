package laboratorio.config;

import laboratorio.model.dto.request.ExamenTomadoRequestDTO;
import laboratorio.model.entity.ExamenesTomados;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper  = new ModelMapper();
        modelMapper .getConfiguration().setSkipNullEnabled(true);
        modelMapper .typeMap(ExamenTomadoRequestDTO.class, ExamenesTomados.class)
                .addMappings(mapper -> {
                    mapper.map(ExamenTomadoRequestDTO::getHistoria, ExamenesTomados::setDocumento);
                    mapper.map(ExamenTomadoRequestDTO::getHistoria, ExamenesTomados::setHistoria);
                    mapper.map(ExamenTomadoRequestDTO::getNumeroFolio, ExamenesTomados::setFolio);
                    mapper.map(ExamenTomadoRequestDTO::getNumeroIngreso, ExamenesTomados::setIngreso);
                });
        return modelMapper ;
    }
}
