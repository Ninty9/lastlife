package io.github.ninty9.lastlife;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.Com
import java.util.concurrent.ThreadLocalRandom;

public interface LivesComponent extends Component {
    void generate(int max, int min);
    int getValue();
    int decrement();
}

class PlayerLivesComponent implements LivesComponent {
    private int value = 9;
    @Override public void generate(int max, int min) {value = ThreadLocalRandom.current().nextInt(min, max + 1);}
    @Override public int getValue() {return this.value;}
    @Override public int decrement() {this.value--;}
    @Override public void readFromNbt(CompoundTag tag)
}