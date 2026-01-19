package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.contract.ContractResponse;
import ru.chousik.is.entity.Contract;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.ContractRepository;
import ru.chousik.is.repository.RentalRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ContractRepository contractRepository;
    private final RentalRepository rentalRepository;

    @Transactional(readOnly = true)
    public Contract getContractForRental(UUID rentalId, UUID userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental %s not found".formatted(rentalId)));
        ensureParticipant(rental, userId);
        return contractRepository.findFirstByRental_IdOrderBySignedAtDesc(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract for rental %s not found".formatted(rentalId)));
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractResponse(UUID rentalId, UUID userId) {
        Contract contract = getContractForRental(rentalId, userId);
        return new ContractResponse(
                contract.getId(),
                contract.getRental() != null ? contract.getRental().getId() : null,
                contract.getStatus(),
                contract.getSignedAt(),
                contract.getFileUrl(),
                contract.getSignatureHash()
        );
    }

    @Transactional(readOnly = true)
    public String renderContractText(UUID rentalId, UUID userId) {
        Contract contract = getContractForRental(rentalId, userId);
        Rental rental = contract.getRental();
        Listing listing = rental != null ? rental.getListing() : null;
        User lessor = rental != null ? rental.getLessor() : null;
        User lessee = rental != null ? rental.getLessee() : null;
        String city = safe(lessee != null ? lessee.getCity() : (lessor != null ? lessor.getCity() : null));
        String contractDate = formatDate(contract.getSignedAt());
        String periodStart = formatDate(rental != null ? rental.getStartAt() : null);
        String periodEnd = formatDate(rental != null ? rental.getEndAt() : null);
        String totalAmount = formatMoney(rental != null ? rental.getTotalAmount() : null);
        String depositAmount = formatMoney(rental != null ? rental.getDepositAmount() : null);

        StringBuilder sb = new StringBuilder();
        sb.append("ДОГОВОР АРЕНДЫ ДВИЖИМОГО ИМУЩЕСТВА № ").append(rentalId).append('\n');
        sb.append("г. ").append(city).append(" «___» __________ 20___ г.").append('\n');
        sb.append("Дата подписания: ").append(contractDate).append('\n');
        sb.append('\n');
        sb.append(formatUserSide("Арендодатель", lessor)).append('\n');
        sb.append(formatUserSide("Арендатор", lessee)).append('\n');
        sb.append('\n');
        sb.append("1. Предмет договора").append('\n');
        sb.append("1.1. Арендодатель обязуется предоставить во временное владение и пользование Арендатору имущество, ")
                .append("указанное в приложении № 1 (перечень имущества) к настоящему договору (далее — Имущество), ")
                .append("на основании акта приема-передачи. Имущество предоставляется Арендатору для использования в следующих целях: ____________________.").append('\n');
        sb.append("1.2. Арендодатель гарантирует, что передаваемое Имущество не обременено правами третьих лиц, в споре или под арестом не находится.").append('\n');
        sb.append('\n');
        sb.append("2. Сумма договора и порядок расчетов").append('\n');
        sb.append("2.1. Размер арендной платы за весь период аренды составляет ").append(totalAmount).append('.').append('\n');
        sb.append("2.2. Залог составляет ").append(depositAmount).append('.').append('\n');
        sb.append("2.3. Оплата производится Арендатором в порядке, согласованном Сторонами, посредством безналичного перевода.").append('\n');
        sb.append('\n');
        sb.append("3. Срок аренды и действия договора").append('\n');
        sb.append("3.1. Срок аренды: с ").append(periodStart).append(" по ").append(periodEnd).append('.').append('\n');
        sb.append("3.2. Договор вступает в силу с момента его подписания Сторонами и действует до полного исполнения обязательств.").append('\n');
        sb.append('\n');
        sb.append("4. Обязанности Арендодателя").append('\n');
        sb.append("4.1. Передать Арендатору Имущество в состоянии, отвечающем условиям настоящего договора, по акту приема-передачи (приложение № 2).").append('\n');
        sb.append("4.2. В присутствии Арендатора проверить исправность Имущества и ознакомить его с правилами эксплуатации.").append('\n');
        sb.append("4.3. Оказывать консультативную помощь в целях наиболее эффективного использования Имущества.").append('\n');
        sb.append('\n');
        sb.append("5. Обязанности Арендатора").append('\n');
        sb.append("5.1. Произвести оплату в срок согласно п. 2.3 настоящего договора.").append('\n');
        sb.append("5.2. Использовать Имущество в соответствии с условиями договора и назначением Имущества.").append('\n');
        sb.append("5.3. Поддерживать Имущество в исправном состоянии.").append('\n');
        sb.append("5.4. Возвратить Имущество Арендодателю после прекращения договора по акту возврата (приложение № 3) в том состоянии, в каком оно было передано, с учетом нормального износа.").append('\n');
        sb.append("5.5. Сдача в субаренду Имущества, передача прав и обязанностей по настоящему договору не допускаются.").append('\n');
        sb.append('\n');
        sb.append("6. Ответственность сторон").append('\n');
        sb.append("6.1. Ответственность сторон определяется в соответствии с законодательством Российской Федерации.").append('\n');
        sb.append("6.2. В случае выхода из строя Имущества по причинам, не зависящим от Арендатора, Арендодатель обязан устранить поломку или заменить неисправный предмет.").append('\n');
        sb.append('\n');
        sb.append("7. Обстоятельства непреодолимой силы").append('\n');
        sb.append("7.1. При наступлении обстоятельств невозможности исполнения обязательств по договору срок исполнения переносится на период действия таких обстоятельств.").append('\n');
        sb.append("7.2. Сторона, для которой создалась невозможность исполнения обязательств, обязана уведомить другую сторону в письменной форме.").append('\n');
        sb.append('\n');
        sb.append("8. Разрешение споров").append('\n');
        sb.append("8.1. Споры по договору решаются путем переговоров, а при недостижении соглашения — в судебном порядке.").append('\n');
        sb.append("8.2. До предъявления иска обязательна претензия, рассматриваемая в 20-дневный срок.").append('\n');
        sb.append('\n');
        sb.append("9. Прочие условия").append('\n');
        sb.append("9.1. Договор составлен в двух экземплярах, по одному для каждой из сторон.").append('\n');
        sb.append("9.2. Передача прав и обязанностей третьим лицам допускается только с письменного согласия другой стороны.").append('\n');
        sb.append("9.3. Стороны обязуются уведомлять друг друга об изменении контактных данных.").append('\n');
        sb.append('\n');
        sb.append("10. Контактные данные сторон").append('\n');
        sb.append(formatUserContacts("Арендодатель", lessor)).append('\n');
        sb.append(formatUserContacts("Арендатор", lessee)).append('\n');
        sb.append('\n');
        sb.append("Приложение № 1").append('\n');
        sb.append("Перечень имущества").append('\n');
        sb.append("1) ").append(listing != null ? safe(listing.getTitle()) : "—")
                .append(", адрес: ").append(listing != null ? safe(listing.getAddress()) : "—").append(".").append('\n');
        sb.append("Общая стоимость: ").append(totalAmount).append('.').append('\n');
        sb.append('\n');
        sb.append("Приложение № 2").append('\n');
        sb.append("Акт приема-передачи имущества в аренду").append('\n');
        sb.append("Стороны подтверждают передачу Имущества в исправном состоянии.").append('\n');
        sb.append('\n');
        sb.append("Приложение № 3").append('\n');
        sb.append("Акт возврата арендованного имущества").append('\n');
        sb.append("Стороны подтверждают возврат Имущества в удовлетворительном состоянии с учетом нормального износа.").append('\n');
        sb.append('\n');
        sb.append("Электронная подпись: ").append(contract.getSignatureHash() != null ? contract.getSignatureHash() : "—");
        return sb.toString();
    }

    private void ensureParticipant(Rental rental, UUID userId) {
        if (rental == null) {
            throw new ResourceNotFoundException("Rental not found");
        }
        UUID lessorId = rental.getLessor() != null ? rental.getLessor().getId() : null;
        UUID lesseeId = rental.getLessee() != null ? rental.getLessee().getId() : null;
        if (!Objects.equals(lessorId, userId) && !Objects.equals(lesseeId, userId)) {
            throw new BusinessValidationException("User is not a participant of rental");
        }
    }

    private String formatUser(User user) {
        if (user == null) {
            return "—";
        }
        String name = (safe(user.getName()) + " " + safe(user.getSurname())).trim();
        String username = user.getUsername() != null ? "(@" + user.getUsername() + ")" : "";
        return (name.isBlank() ? "Пользователь" : name) + " " + username + " [" + user.getId() + "]";
    }

    private String formatUserSide(String role, User user) {
        return role + ": " + formatUser(user);
    }

    private String formatUserContacts(String role, User user) {
        if (user == null) {
            return role + ": —";
        }
        String name = (safe(user.getName()) + " " + safe(user.getSurname())).trim();
        String email = safe(user.getEmail());
        String phone = safe(user.getPhone());
        return role + ": " + (name.isBlank() ? "Пользователь" : name)
                + ", email: " + email
                + ", телефон: " + phone;
    }

    private String formatDate(OffsetDateTime value) {
        return value != null ? DATE.format(value) : "—";
    }

    private String formatDateTime(OffsetDateTime value) {
        return value != null ? DATE_TIME.format(value) : "—";
    }

    private String formatMoney(BigDecimal value) {
        return value != null ? value.toPlainString() + " ₽" : "—";
    }

    private String safe(String value) {
        return value == null ? "—" : value;
    }
}
