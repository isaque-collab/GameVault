import {
    useEffect,
    useState,
    type SubmitEvent,
} from 'react'
import {useSearchParams} from 'react-router'
import {buscarJogos} from '../../api/jogosApi.ts'
import GameSection, {
    type GameSectionItem,
} from '../../components/game/GameSection/GameSection.tsx'
import type {
    JogoResumo,
    OrdenacaoJogo,
} from '../../types/jogo.ts'
import './CatalogoPage.css'

const TAMANHO_PAGINA = 20

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

function obterPagina(valor: string | null): number {
    const pagina = Number(valor)
    return Number.isInteger(pagina) && pagina >= 1 ? pagina : 1
}

function obterOrdenacao(
    valor: string | null,
): OrdenacaoJogo | undefined {
    const ordenacoes: OrdenacaoJogo[] = [
        'POPULARIDADE',
        'AVALIACAO_RAWG',
        'METACRITIC',
        'LANCAMENTO',
        'NOME',
    ]

    return ordenacoes.find((ordenacao) => ordenacao === valor)
}

function CatalogoPage() {
    const [searchParams, setSearchParams] = useSearchParams()

    const nomeAplicado = searchParams.get('nome') ?? ''
    const generoAplicado = searchParams.get('genero') ?? ''
    const ordenacaoAplicada = obterOrdenacao(
        searchParams.get('ordenacao'),
    )
    const pagina = obterPagina(searchParams.get('pagina'))

    const [jogos, setJogos] = useState<GameSectionItem[]>([])
    const [totalResultados, setTotalResultados] = useState(0)
    const [carregando, setCarregando] = useState(true)
    const [erro, setErro] = useState<string | null>(null)

    useEffect(() => {
        async function carregarCatalogo() {
            try {
                setCarregando(true)
                setErro(null)

                const resposta = await buscarJogos({
                    nome: nomeAplicado || undefined,
                    genero: generoAplicado || undefined,
                    ordenacao: ordenacaoAplicada,
                    pagina,
                })

                setJogos(
                    resposta.jogos.map(converterParaGameSectionItem),
                )
                setTotalResultados(resposta.totalResultados)
            } catch (error) {
                console.error(error)
                setErro('Não foi possível carregar o catálogo.')
            } finally {
                setCarregando(false)
            }
        }

        carregarCatalogo()
    }, [nomeAplicado, generoAplicado, ordenacaoAplicada, pagina])

    function pesquisar(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()

        const dados = new FormData(event.currentTarget)

        const nome =
            String(dados.get('nome') ?? '').trim()

        const genero =
            String(dados.get('genero') ?? '').trim()

        const ordenacao =
            String(dados.get('ordenacao') ?? '')

        const parametros =
            new URLSearchParams()

        if (nome) {
            parametros.set('nome', nome)
        }

        if (genero) {
            parametros.set('genero', genero)
        }

        if (ordenacao) {
            parametros.set(
                'ordenacao',
                ordenacao,
            )
        }

        parametros.set('pagina', '1')

        setSearchParams(parametros)
    }

    function limparFiltros() {
        setSearchParams({pagina: '1'})
    }

    function irParaPagina(novaPagina: number) {
        const parametros = new URLSearchParams(searchParams)
        parametros.set('pagina', String(novaPagina))
        setSearchParams(parametros)
    }

    const totalPaginas = Math.max(
        1,
        Math.ceil(totalResultados / TAMANHO_PAGINA),
    )

    return (
        <main className="catalogo-page">
            <h1>Catálogo</h1>

            <form
                key={searchParams.toString()}
                className="catalogo-filtros"
                onSubmit={pesquisar}>
                <div className="catalogo-campo">
                    <label htmlFor="catalogo-nome">Nome</label>
                    <input
                        id="catalogo-nome"
                        name="nome"
                        type="search"
                        defaultValue={nomeAplicado}
                        placeholder="Buscar jogo"
                    />
                </div>

                <div className="catalogo-campo">
                    <label htmlFor="catalogo-genero">Gênero</label>
                    <input
                        id="catalogo-genero"
                        name="genero"
                        type="text"
                        defaultValue={generoAplicado}
                        placeholder="Ex.: action"
                    />
                </div>

                <div className="catalogo-campo">
                    <label htmlFor="catalogo-ordenacao">
                        Ordenar por
                    </label>
                    <select
                        id="catalogo-ordenacao"
                        name="ordenacao"
                        defaultValue={
                        ordenacaoAplicada ?? ''
                        }
                    >
                        <option value="">Padrão</option>
                        <option value="POPULARIDADE">Popularidade</option>
                        <option value="AVALIACAO_RAWG">
                            Avaliação RAWG
                        </option>
                        <option value="METACRITIC">Metacritic</option>
                        <option value="LANCAMENTO">Lançamento</option>
                        <option value="NOME">Nome</option>
                    </select>
                </div>

                <div className="catalogo-acoes">
                    <button type="submit">Pesquisar</button>
                    <button type="button" onClick={limparFiltros}>
                        Limpar filtros
                    </button>
                </div>
            </form>

            {carregando ? (
                <p>Carregando catálogo...</p>
            ) : erro ? (
                <p role="alert">{erro}</p>
            ) : jogos.length === 0 ? (
                <p>Nenhum jogo encontrado.</p>
            ) : (
                <>
                    <GameSection titulo="Jogos" jogos={jogos}/>

                    <div className="catalogo-paginacao">
                        <button
                            type="button"
                            onClick={() => irParaPagina(pagina - 1)}
                            disabled={pagina <= 1}
                        >
                            Anterior
                        </button>

                        <button
                            type="button"
                            onClick={() => irParaPagina(pagina + 1)}
                            disabled={pagina >= totalPaginas}
                        >
                            Próxima
                        </button>
                    </div>
                </>
            )}
        </main>
    )
}

export default CatalogoPage