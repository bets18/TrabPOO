package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class CidadeTest {

    @Test(timeout = 2000)
    public void testaSeCidadeArmazenaTesourosCorretamente() {
        Cidade cidade = new Cidade();
        
        cidade.tesouros.add(100);
        cidade.tesouros.add(200);
        cidade.tesouros.add(300);
        cidade.tesouros.add(400);
        
        assertEquals("A cidade deveria armazenar 4 tesouros na sua lista.", 4, cidade.tesouros.size());
    }
}
