package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PET_INDEX;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CLIENTS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PETS;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.association.exceptions.ClientPetAssociationNotFoundException;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;

//@@author jonathanwj-unused
// Entire command was merged into AddPetCommand that
// currently creates a pet and adds that pet to the client in one command.
/**
 * Edits the details of an existing person in the address book.
 */
public class RemovePetFromClientCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "removepetfromclient";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Remove a pet from an owner "
            + "by the index number used in the last client and pet listing.\n"
            + "Parameters: "
            + PREFIX_PET_INDEX + "PET_INDEX "
            + PREFIX_CLIENT_INDEX + "CLIENT_INDEX\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_PET_INDEX + "1 " + PREFIX_CLIENT_INDEX + "2";

    public static final String MESSAGE_ADD_PET_TO_CLIENT_SUCCESS = "Removed Pet from client:\n%1$s\n>> %2$s";
    public static final String MESSAGE_CLIENT_PET_NOT_ASSOCIATED = "Client is not the owner of the indicated pet";

    private final Index petIndex;
    private final Index clientIndex;

    private Optional<Pet> pet;
    private Optional<Client> client;

    /**
     * @param petIndex of the pet in the filtered pet list to add
     * @param clientIndex of the person in the filtered client list to add pet to
     */
    public RemovePetFromClientCommand(Index petIndex, Index clientIndex) {
        requireNonNull(petIndex);
        requireNonNull(clientIndex);

        this.petIndex = petIndex;
        this.clientIndex = clientIndex;

        pet = Optional.empty();
        client = Optional.empty();
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(model);
        requireNonNull(pet.get());
        requireNonNull(client.get());
        try {
            model.removePetFromClient(pet.get(), client.get());
        } catch (ClientPetAssociationNotFoundException e) {
            throw new CommandException(MESSAGE_CLIENT_PET_NOT_ASSOCIATED);
        }
        return new CommandResult(String.format(MESSAGE_ADD_PET_TO_CLIENT_SUCCESS, pet.get(), client.get()));

    }

    @Override
    protected void preprocessUndoableCommand() throws CommandException {
        List<Pet> lastShownListPet = model.getFilteredPetList();
        List<Client> lastShownListClient = model.getFilteredClientList();

        if (clientIndex.getZeroBased() >= lastShownListClient.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        if (petIndex.getZeroBased() >= lastShownListPet.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PET_DISPLAYED_INDEX);
        }

        pet = Optional.of(lastShownListPet.get(petIndex.getZeroBased()));
        client = Optional.of(lastShownListClient.get(clientIndex.getZeroBased()));
        model.updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);
        model.updateFilteredPetList(PREDICATE_SHOW_ALL_PETS);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof RemovePetFromClientCommand)) {
            return false;
        }

        // state check
        RemovePetFromClientCommand e = (RemovePetFromClientCommand) other;
        return petIndex.equals(e.petIndex)
                && clientIndex.equals(e.clientIndex)
                && pet.equals(e.pet)
                && client.equals(e.client);
    }

}
