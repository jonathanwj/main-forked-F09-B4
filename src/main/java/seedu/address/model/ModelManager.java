package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.AddressBookChangedEvent;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.exceptions.DuplicateAppointmentException;
import seedu.address.model.client.Client;
import seedu.address.model.client.exceptions.ClientNotFoundException;
import seedu.address.model.client.exceptions.DuplicateClientException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.vettechnician.VetTechnician;
import seedu.address.model.vettechnician.exceptions.DuplicateVetTechnicianException;
import seedu.address.model.vettechnician.exceptions.VetTechnicianNotFoundException;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final FilteredList<Person> filteredPersons;
    private final FilteredList<Client> filteredClients;
    private final FilteredList<VetTechnician> filteredVetTechnicians;
    private final FilteredList<Pet> filteredPet;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, UserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        filteredClients = new FilteredList<>(this.addressBook.getClientList());
        filteredVetTechnicians = new FilteredList<>(this.addressBook.getVetTechnicianList());
        filteredPet = new FilteredList<>((this.addressBook.getPetList()));
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyAddressBook newData) {
        addressBook.resetData(newData);
        indicateAddressBookChanged();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateAddressBookChanged() {
        raise(new AddressBookChangedEvent(addressBook));
    }

    //Person

    @Override
    public synchronized void deletePerson(Person target) throws PersonNotFoundException {
        addressBook.removePerson(target);

        try {
            if (target.isClient()) {
                deleteClient((Client) target);
            } else {
                deleteVetTechnician((VetTechnician) target);
            }
        } catch (ClientNotFoundException | VetTechnicianNotFoundException e) {
            throw new PersonNotFoundException();
        }
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void addPerson(Person person) throws DuplicatePersonException {
        addressBook.addPerson(person);

        try {
            if (person.isClient()) {
                addClient((Client) person);
            } else {
                addVetTechnician((VetTechnician) person);
            }
        } catch (DuplicateClientException | DuplicateVetTechnicianException e) {
            throw new DuplicatePersonException();
        }
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void scheduleAppointment(Appointment appointment) throws DuplicateAppointmentException {
        addressBook.scheduleAppointment(appointment);
        indicateAddressBookChanged();
    }

    @Override
    public void updatePerson(Person target, Person editedPerson)
            throws DuplicatePersonException, PersonNotFoundException {
        requireAllNonNull(target, editedPerson);

        addressBook.updatePerson(target, editedPerson);
        try {
            if (target.isClient() && !editedPerson.isClient()) {
                deleteClient((Client) target);
                addVetTechnician((VetTechnician) editedPerson);
            } else if (!target.isClient() && editedPerson.isClient()) {
                deleteVetTechnician((VetTechnician) target);
                addClient((Client) editedPerson);
            } else if (target.isClient()) {
                updateClient((Client) target, (Client) editedPerson);
            } else if (!target.isClient()) {
                updateVetTechnician((VetTechnician) target, (VetTechnician) editedPerson);
            }
        } catch (DuplicateVetTechnicianException | DuplicateClientException e) {
            throw new DuplicatePersonException();
        } catch (ClientNotFoundException | VetTechnicianNotFoundException e) {
            throw new PersonNotFoundException();
        }
        indicateAddressBookChanged();
    }

    //Client

    /**
     *  Deletes the given Client
     */
    private synchronized void deleteClient(Client target) throws ClientNotFoundException {
        addressBook.removeClient(target);
        indicateAddressBookChanged();
    }


    /**
     *  Adds the given Client
     */
    private synchronized void addClient(Client person) throws DuplicateClientException {
        addressBook.addClient(person);
        updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);
        indicateAddressBookChanged();
    }

    /**
     *  Update target Client with a Client
     */
    private void updateClient(Client target, Client editedClient)
            throws DuplicateClientException, ClientNotFoundException {
        requireAllNonNull(target, editedClient);

        addressBook.updateClient(target, editedClient);
        indicateAddressBookChanged();
    }

    // VetTechnician

    /**
     *  Deletes the given Vet Technician
     */
    private synchronized void deleteVetTechnician(VetTechnician target) throws VetTechnicianNotFoundException {
        addressBook.removeVetTechnician(target);
        indicateAddressBookChanged();
    }

    /**
     *  Adds the given Vet Technician
     */
    private synchronized void addVetTechnician(VetTechnician person) throws DuplicateVetTechnicianException {
        addressBook.addVetTechnician(person);
        updateFilteredVetTechnicianList(PREDICATE_SHOW_ALL_TECHNICIAN);
        indicateAddressBookChanged();
    }

    /**
     *  Update Vet Technician with a Vet Technician
     */
    private void updateVetTechnician(VetTechnician target, VetTechnician editedVetTechnician)
            throws DuplicateVetTechnicianException, VetTechnicianNotFoundException {
        requireAllNonNull(target, editedVetTechnician);

        addressBook.updateVetTechnician(target, editedVetTechnician);
        indicateAddressBookChanged();
    }

    // Pet

    @Override
    public synchronized void addPet(Pet pet) throws DuplicatePetException {
        addressBook.addPet(pet);
        indicateAddressBookChanged();
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return FXCollections.unmodifiableObservableList(filteredPersons);
    }

    /**
     * Returns an unmodifiable view of the list of {@code Pet} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Pet> getFilteredPetList() {
        return FXCollections.unmodifiableObservableList(filteredPet);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    //Client

    /**
     * Returns an unmodifiable view of the list of {@code Client} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Client> getFilteredClientList() {
        return FXCollections.unmodifiableObservableList(filteredClients);
    }

    @Override
    public void updateFilteredClientList(Predicate<Client> predicate) {
        requireNonNull(predicate);
        filteredClients.setPredicate(predicate);
    }

    //Vet Technician

    /**
     * Returns an unmodifiable view of the list of {@code VetTechnician} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<VetTechnician> getFilteredVetTechnicianList() {
        return FXCollections.unmodifiableObservableList(filteredVetTechnicians);
    }

    @Override
    public void updateFilteredVetTechnicianList(Predicate<VetTechnician> predicate) {
        requireNonNull(predicate);
        filteredVetTechnicians.setPredicate(predicate);
    }

    @Override
    public void updateFilteredPetList(Predicate<Pet> predicate) {
        requireNonNull(predicate);
        filteredPet.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return addressBook.equals(other.addressBook)
                && filteredPersons.equals(other.filteredPersons)
                && filteredClients.equals(other.filteredClients)
                && filteredVetTechnicians.equals(other.filteredVetTechnicians);
    }
}
