package pt.ist.meic.phylodb.typing.schema;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ist.meic.phylodb.phylogeny.locus.LocusRepository;
import pt.ist.meic.phylodb.phylogeny.locus.model.Locus;
import pt.ist.meic.phylodb.typing.schema.model.Schema;
import pt.ist.meic.phylodb.utils.db.EntityRepository;
import pt.ist.meic.phylodb.utils.service.Reference;

import java.util.List;
import java.util.Optional;

@Service
public class SchemaService {

	private LocusRepository locusRepository;
	private SchemaRepository schemaRepository;

	public SchemaService(LocusRepository locusRepository, SchemaRepository schemaRepository) {
		this.locusRepository = locusRepository;
		this.schemaRepository = schemaRepository;
	}

	@Transactional(readOnly = true)
	public Optional<List<Schema>> getSchemas(String taxonId, int page, int limit) {
		return schemaRepository.findAll(page, limit, taxonId);
	}

	@Transactional(readOnly = true)
	public Optional<Schema> getSchema(String taxonId, String schemaId, Long version) {
		return schemaRepository.find(new Schema.PrimaryKey(taxonId, schemaId), version);
	}

	@Transactional
	public boolean saveSchema(Schema schema) {
		Locus.PrimaryKey[] lociKeys = schema.getLociIds().stream()
				.map(r -> new Locus.PrimaryKey(schema.getPrimaryKey().getTaxonId(), r.getPrimaryKey()))
				.toArray(Locus.PrimaryKey[]::new);
		Optional<Schema> dbSchema = schemaRepository.find(schema);
		if (locusRepository.anyMissing(lociKeys) ||
				(dbSchema.isPresent() && !dbSchema.get().getPrimaryKey().equals(schema.getPrimaryKey())))
			return false;
		return schemaRepository.save(schema);
	}

	@Transactional
	public boolean deleteSchema(String taxonId, String schemaId) {
		return schemaRepository.remove(new Schema.PrimaryKey(taxonId, schemaId));
	}

}
