package fr.ecosort.backend.services;

import fr.ecosort.backend.models.Admin;
import fr.ecosort.backend.models.Conseil;
import fr.ecosort.backend.repositories.AdminRepository;
import fr.ecosort.backend.repositories.ConseilRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConseilService {

    private final ConseilRepository conseilRepository;
    private final AdminRepository   adminRepository;

    public ConseilService(ConseilRepository conseilRepository,
                          AdminRepository adminRepository) {
        this.conseilRepository = conseilRepository;
        this.adminRepository   = adminRepository;
    }

    public List<Conseil> getAll() {
        return conseilRepository.findAll();
    }

    public Conseil create(String titre, String description, String idAdmin) {
        UUID uuid = UUID.fromString(idAdmin);
        Admin admin = adminRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Admin introuvable : " + idAdmin));
        Conseil conseil = new Conseil(titre, description);
        conseil.setAdmin(admin);
        Conseil saved = conseilRepository.save(conseil);
        // Retourner un objet propre sans relation circulaire
        return toClean(saved);
    }

    public Conseil update(int id, String titre, String description) {
        Conseil conseil = conseilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conseil introuvable : " + id));
        conseil.setTitre(titre);
        conseil.setDescription(description);
        Conseil saved = conseilRepository.save(conseil);
        return toClean(saved);
    }

    public void delete(int id) {
        conseilRepository.deleteById(id);
    }

    // Retourne un Conseil sans les relations pour éviter boucle JSON
    private Conseil toClean(Conseil c) {
        Conseil clean = new Conseil(c.getTitre(), c.getDescription());
        // Hack pour setter l'id (champ privé généré)
        try {
            java.lang.reflect.Field f = Conseil.class.getDeclaredField("idConseil");
            f.setAccessible(true);
            f.set(clean, c.getIdConseil());
        } catch (Exception ignored) {}
        return clean;
    }
}