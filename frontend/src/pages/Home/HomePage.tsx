import {useEffect, useState} from 'react'
import {
    buscarJogosMaisBemAvaliados,
    buscarJogosPopulares,
    buscarLancamentosRecentes,
} from '../../api/jogosApi.ts'
import GameSection, {
    type GameSectionItem,
} from '../../components/game/GameSection/GameSection.tsx'
import type {JogoResumo} from '../../types/jogo.ts';
import './HomePage.css'

function converterParaGameSectionItem(
    jogo: JogoResumo,
): GameSectionItem {
    return {
        id: jogo.rawgGameId,
        nome: jogo.nome,
        imagemUrl: jogo.imagemFundo ?? undefined,
        avaliacao: jogo.notaRawg ?? undefined,
        anoLancamento: jogo.dataLancamento
            ? Number(jogo.dataLancamento.slice(0, 4))
            : undefined,
    }
}

function HomePage() {
    const [populares, setPopulares] = useState<GameSectionItem[]>([])
    const [lancamentos, setLancamentos] = useState<GameSectionItem[]>([])
    const [maisBemAvaliados, setMaisBemAvaliados] =
        useState<GameSectionItem[]>([])

    const [carregando, setCarregando] = useState(true)
    const [erro, setErro] = useState<string | null>(null)

    useEffect(() => {
        async function carregarJogos() {
            try {
                setCarregando(true)
                setErro(null)

                const [
                    respostaPopulares,
                    respostaLancamentos,
                    respostaMaisBemAvaliados,
                ] = await Promise.all([
                    buscarJogosPopulares(),
                    buscarLancamentosRecentes(),
                    buscarJogosMaisBemAvaliados(),
                ])

                setPopulares(
                    respostaPopulares.jogos.map(converterParaGameSectionItem),
                )

                setLancamentos(
                    respostaLancamentos.jogos.map(converterParaGameSectionItem),
                )

                setMaisBemAvaliados(
                    respostaMaisBemAvaliados.jogos.map(
                        converterParaGameSectionItem,
                    ),
                )
            } catch (error) {
                console.error(error)
                setErro('Não foi possível carregar os jogos.')
            } finally {
                setCarregando(false)
            }
        }

        carregarJogos()
    }, [])

    if (carregando) {
        return (
            <main className="home">
                <p>Carregando jogos...</p>
            </main>
        )
    }

    if (erro) {
        return (
            <main className="home">
                <p>{erro}</p>
            </main>
        )
    }
    return (
        <main className="home">
            <h1>GameVault</h1>
            <p>Sua biblioteca de jogos.</p>

            <GameSection
                titulo="Populares"
                jogos={populares}
            />

            <GameSection
                titulo="Lancamentos recentes"
                jogos={lancamentos}
            />

            <GameSection
                titulo="Melhores avaliados"
                jogos={maisBemAvaliados}
            />
        </main>
    )
}

export default HomePage